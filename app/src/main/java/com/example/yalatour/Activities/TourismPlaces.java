package com.example.yalatour.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.yalatour.Adapters.TourismPlaceAdapter;
import com.example.yalatour.Classes.TourismPlaceClass;
import com.example.yalatour.DetailsActivity.DetailActivity;
import com.example.yalatour.R;
import com.example.yalatour.UploadActivities.UploadPlaceActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class TourismPlaces extends AppCompatActivity {

    private FloatingActionButton addPlace;
    private RecyclerView recyclerView;
    private List<TourismPlaceClass> placeList;
    private List<TourismPlaceClass> filteredplaceList;
    private String cityName;
    private SearchView PlaceSearch;
    TourismPlaceAdapter adapter;
    private static final int UPLOAD_REQUEST_CODE = 123;
    private static final int EDIT_REQUEST_CODE = 1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tourism_places);

        addPlace = findViewById(R.id.Addplace);
        recyclerView = findViewById(R.id.PlacerecyclerView);
        PlaceSearch=findViewById(R.id.PlacesearchView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        cityName = getIntent().getStringExtra("cityName");
        placeList = new ArrayList<>();
        filteredplaceList = new ArrayList<>();
        adapter = new TourismPlaceAdapter(TourismPlaces.this, filteredplaceList, cityName);
        recyclerView.setAdapter(adapter);

        // Fetch and display only places belonging to the selected city
        fetchPlaces();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();

            db.collection("Users").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Boolean isAdmin = documentSnapshot.getBoolean("admin");
                            addPlace.setVisibility(isAdmin != null && isAdmin ? View.VISIBLE : View.GONE);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("TourismPlaces", "Failed to fetch user data", e);
                    });
        } else {
            addPlace.setVisibility(View.GONE);
        }

        addPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TourismPlaces.this, UploadPlaceActivity.class);
                intent.putExtra("cityName", cityName);
                startActivityForResult(intent, UPLOAD_REQUEST_CODE); // Start the activity for result
            }
        });
        PlaceSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterPlaces(newText);
                return true;
            }
        });
    }


    private void fetchPlaces() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query query = db.collection("TourismPlaces").whereEqualTo("cityName", cityName);

        query.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        placeList.clear();
                        filteredplaceList.clear(); // Also clear the filtered list
                        for (DocumentSnapshot document : task.getResult()) {
                            TourismPlaceClass place = document.toObject(TourismPlaceClass.class);
                            if (place != null) {
                                placeList.add(place);
                                filteredplaceList.add(place); // Add to filtered list as well
                            }
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(TourismPlaces.this, "Failed to fetch places", Toast.LENGTH_SHORT).show();
                        Log.e("TourismPlaces", "Error fetching places", task.getException());
                    }
                });
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UPLOAD_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null && data.getBooleanExtra("newPlaceAdded", false)) {
                // Refresh the place list
                fetchPlaces();
            }
        } else if (requestCode == EDIT_REQUEST_CODE && resultCode == RESULT_OK) {
            Log.d("TourismPlaces", "onActivityResult() called with requestCode=" + requestCode + ", resultCode=" + resultCode);
            if (data != null && data.getBooleanExtra("placeEdited", false)) {
                // Refresh the place list when a place is edited
                fetchPlaces();

            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the place list every time the activity resumes
        fetchPlaces();
    }

    public void filterPlaces(String query) {
        filteredplaceList.clear();
        if (query.isEmpty()) {
            filteredplaceList.addAll(placeList);
        } else {
            for (TourismPlaceClass place : placeList) {
                if (place.getPlaceName().toLowerCase().contains(query.toLowerCase())) {
                    filteredplaceList.add(place);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

}
