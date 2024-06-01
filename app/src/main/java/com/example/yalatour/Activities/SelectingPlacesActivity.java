package com.example.yalatour.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yalatour.Adapters.SelectingPlacesAdapter;
import com.example.yalatour.Classes.TourismPlaceClass;
import com.example.yalatour.Classes.TripClass;
import com.example.yalatour.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class SelectingPlacesActivity extends AppCompatActivity {

    RecyclerView selectingPlacesRecyclerView;
    List<TourismPlaceClass> allPlacesList;
    List<TourismPlaceClass> filteredPlaceList; // Changed to instance variable
    FirebaseFirestore db;
    TextView done;
    String tripId;
    SelectingPlacesAdapter adapter;
    List<TourismPlaceClass> selectedPlaces;
    SearchView SelectingPlacesSearchView; // Changed to instance variable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selecting_places);

        selectingPlacesRecyclerView = findViewById(R.id.SlectingPlacesrecyclerView);
        selectingPlacesRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        SelectingPlacesSearchView = findViewById(R.id.SelectingPlacesearchView); // Changed variable name to match XML
        db = FirebaseFirestore.getInstance();
        tripId = getIntent().getStringExtra("TripId");
        allPlacesList = new ArrayList<>();
        done = findViewById(R.id.DoneAddingPlaces);
        selectedPlaces = new ArrayList<>();
        filteredPlaceList = new ArrayList<>(); // Initialize filteredPlaceList
        adapter = new SelectingPlacesAdapter(this, filteredPlaceList, selectedPlaces);
        selectingPlacesRecyclerView.setAdapter(adapter);
        fetchSelectedPlaces();

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.collection("Trips").document(tripId)
                        .update("tripPlaces", adapter.getSelectedPlaces())
                        .addOnSuccessListener(aVoid -> {
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("placesAdded", true);
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Log.e("Activity", "Failed to update selected places: " + e.getMessage());
                        });
            }
        });

        fetchAllPlaces();
        SelectingPlacesSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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

    public void fetchAllPlaces() {
        db.collection("TourismPlaces")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        allPlacesList.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            TourismPlaceClass place = document.toObject(TourismPlaceClass.class);
                            allPlacesList.add(place);
                        }
                        filterPlaces(SelectingPlacesSearchView.getQuery().toString());

                    } else {
                        // Handle errors
                        Log.e("Activity", "Failed to fetch all places: ", task.getException());
                    }
                });
    }

    private void fetchSelectedPlaces() {
        if (tripId != null) {
            db.collection("Trips").document(tripId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            this.selectedPlaces = documentSnapshot.toObject(TripClass.class).getTripPlaces();
                            if (this.selectedPlaces != null && !this.selectedPlaces.isEmpty()) {
                                adapter = new SelectingPlacesAdapter(this, filteredPlaceList, selectedPlaces); // Pass filteredPlaceList
                                selectingPlacesRecyclerView.setAdapter(adapter);
                                adapter.notifyDataSetChanged();
                                Log.d("Activity", "Selected places from Firestore: " + this.selectedPlaces.toString());
                            } else {
                                adapter = new SelectingPlacesAdapter(SelectingPlacesActivity.this, filteredPlaceList, new ArrayList<>());
                                selectingPlacesRecyclerView.setAdapter(adapter);
                            }
                        } else {
                            // Handle document not found
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Activity", "Failed to fetch selected places: " + e.getMessage());
                    });
        }
    }
    public void filterPlaces(String query) {

            filteredPlaceList.clear();


        String lowerCaseQuery = query.toLowerCase();
        for (TourismPlaceClass place : allPlacesList) {
            if (place.getPlaceName().toLowerCase().contains(lowerCaseQuery) || place.getCityName().toLowerCase().contains(lowerCaseQuery)) {
                filteredPlaceList.add(place);
            }
        }

        adapter.notifyDataSetChanged();
    }
}
