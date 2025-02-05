package com.example.yalatour.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.annotation.NonNull;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.yalatour.Adapters.TourismPlaceAdapter;
import com.example.yalatour.Classes.TourismPlaceClass;
import com.example.yalatour.DetailsActivity.DetailActivity;
import com.example.yalatour.R;
import com.example.yalatour.UploadActivities.UploadPlaceActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class TourismPlaces extends AppCompatActivity {

    private FloatingActionButton addPlace;
    private RecyclerView recyclerView;
    private List<TourismPlaceClass> placeList;
    private List<TourismPlaceClass> filteredplaceList;
    private String cityId;
    private SearchView PlaceSearch;

    ImageButton PlaceBackButton;
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
        PlaceBackButton = findViewById(R.id.PlaceBackButton);
        cityId = getIntent().getStringExtra("cityId");
        Log.d("TourismPlaces", "cityId: " + cityId);
        placeList = new ArrayList<>();
        filteredplaceList = new ArrayList<>();
        adapter = new TourismPlaceAdapter(TourismPlaces.this, filteredplaceList, cityId);
        recyclerView.setAdapter(adapter);

        // Fetch and display only places belonging to the selected city
        fetchPlaces();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        ImageButton touristAttractionButton = findViewById(R.id.TouristAttraction);
        ImageButton museumsButton = findViewById(R.id.Museums);
        ImageButton religiousButton = findViewById(R.id.Religious);
        ImageButton activitiesButton = findViewById(R.id.Activities);
        ImageButton natureButton = findViewById(R.id.Nature);
        Button allButton = findViewById(R.id.All);

        allButton.setOnClickListener(view -> fetchPlaces());
        touristAttractionButton.setOnClickListener(view -> fetchPlacesByCategory("Tourist Attractions"));
        museumsButton.setOnClickListener(view -> fetchPlacesByCategory("Museums"));
        religiousButton.setOnClickListener(view -> fetchPlacesByCategory("Religious Sites"));
        activitiesButton.setOnClickListener(view -> fetchPlacesByCategory("Activities"));
        natureButton.setOnClickListener(view -> fetchPlacesByCategory("Nature"));
        
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
        PlaceBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        addPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TourismPlaces.this, UploadPlaceActivity.class);
                intent.putExtra("cityId", cityId);
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

    private void fetchPlacesByCategory(String category) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("TourismPlaces")
                .whereArrayContains("placeCategories", category)
                .whereEqualTo("cityId", cityId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        placeList.clear();
                        filteredplaceList.clear(); // Clear the filtered list before adding new items
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            TourismPlaceClass place = document.toObject(TourismPlaceClass.class);
                            placeList.add(place);
                            filteredplaceList.add(place);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(TourismPlaces.this, "Error getting places: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void fetchPlaces() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query query = db.collection("TourismPlaces").whereEqualTo("cityId", cityId);

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
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        bottomNav.getMenu().setGroupCheckable(0, true, false);
        for (int i = 0; i < bottomNav.getMenu().size(); i++) {
            bottomNav.getMenu().getItem(i).setChecked(false);
        }
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
    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Intent intent = null;

                    if (item.getItemId() == R.id.navigation_home) {
                        intent = new Intent(TourismPlaces.this, HomePage.class);
                    } else if (item.getItemId() == R.id.navigation_trips) {
                        intent = new Intent(TourismPlaces.this, TripActivity.class);
                    } else if (item.getItemId() == R.id.navigation_cities) {
                        intent = new Intent(TourismPlaces.this, CityActivity.class);
                    } else if (item.getItemId() == R.id.navigation_favorites) {
                        intent = new Intent(TourismPlaces.this,FavoritePage.class);
                    } else if (item.getItemId() == R.id.navigation_profile) {
                        intent = new Intent(TourismPlaces.this, ProfileActivity.class);
                    }

                    if (intent != null) {
                        intent.putExtra("menuItemId", item.getItemId());
                        startActivity(intent);
                        overridePendingTransition(0, 0); // No animation
                        return true;
                    }

                    return false;
                }
            };
}
