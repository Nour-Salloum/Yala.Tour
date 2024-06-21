package com.example.yalatour.Activities;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.media3.common.C;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.example.yalatour.Adapters.MyAdapter;
import com.example.yalatour.Adapters.TourismPlaceAdapter;
import com.example.yalatour.Classes.CityClass;
import com.example.yalatour.Classes.TourismPlaceClass;
import com.example.yalatour.R;
import com.example.yalatour.UploadActivities.UploadActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class CityActivity extends AppCompatActivity {

    private FloatingActionButton fab;
    private RecyclerView cityRecyclerView;
    private RecyclerView placeRecyclerView;
    private List<CityClass> cityList;
    private List<CityClass> filteredCityList;
    private List<TourismPlaceClass> placeList;
    private List<TourismPlaceClass> filteredPlaceList;
    private SearchView searchView;
    private FirebaseFirestore db;
    private MyAdapter cityAdapter;
    private TourismPlaceAdapter placeAdapter;
    private static final int EDITCity_REQUEST_CODE = 12;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city);

        fab = findViewById(R.id.fab);
        cityRecyclerView = findViewById(R.id.cityRecyclerView);
        placeRecyclerView = findViewById(R.id.placeRecyclerView);
        searchView = findViewById(R.id.searchView);
        db = FirebaseFirestore.getInstance();
        cityList = new ArrayList<>();
        filteredCityList = new ArrayList<>();
        placeList = new ArrayList<>();
        filteredPlaceList = new ArrayList<>();

        cityAdapter = new MyAdapter(this, filteredCityList);
        placeAdapter = new TourismPlaceAdapter(this, filteredPlaceList);

        cityRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        placeRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        cityRecyclerView.setAdapter(cityAdapter);
        placeRecyclerView.setAdapter(placeAdapter);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        bottomNav.getMenu().setGroupCheckable(0, true, false);
        for (int i = 0; i < bottomNav.getMenu().size(); i++) {
            bottomNav.getMenu().getItem(i).setChecked(false);
        }

        setupFab();
        //fetchCitiesAndPlaces();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterData(newText);
                return true;
            }
        });
    }

    private void setupFab() {
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(CityActivity.this, UploadActivity.class);
            startActivity(intent);
        });

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("Users").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Boolean isAdmin = documentSnapshot.getBoolean("admin");
                            fab.setVisibility(isAdmin != null && isAdmin ? View.VISIBLE : View.GONE);
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Handle error
                    });
        } else {
            fab.setVisibility(View.GONE);
        }
    }

    private void fetchCitiesAndPlaces() {
        // Clear lists before fetching data
        cityList.clear();
        filteredCityList.clear();
        placeList.clear();
        filteredPlaceList.clear();

        db.collection("Cities")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            CityClass city = document.toObject(CityClass.class);
                            cityList.add(city);
                        }
                        // Add all cities to filtered list
                        filteredCityList.addAll(cityList);
                        cityAdapter.notifyDataSetChanged(); // Notify adapter of changes
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });

        db.collection("TourismPlaces")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            TourismPlaceClass place = document.toObject(TourismPlaceClass.class);
                            placeList.add(place);
                        }
                        // Add all places to filtered list
                        filteredPlaceList.addAll(placeList);
                        placeAdapter.notifyDataSetChanged(); // Notify adapter of changes
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }



    private void filterData(String query) {
        filteredCityList.clear();
        filteredPlaceList.clear();

        if (query.isEmpty()) {
            filteredCityList.addAll(cityList);
            placeRecyclerView.setVisibility(View.GONE);
            cityRecyclerView.setVisibility(View.VISIBLE);
        } else {
            for (CityClass city : cityList) {
                if (city.getCityTitle().toLowerCase().contains(query.toLowerCase())) {
                    filteredCityList.add(city);
                }
            }

            for (TourismPlaceClass place : placeList) {
                if (place.getPlaceName().toLowerCase().contains(query.toLowerCase())) {
                    filteredPlaceList.add(place);
                }
            }

            if (!filteredCityList.isEmpty() && !filteredPlaceList.isEmpty()) {
                cityRecyclerView.setVisibility(View.VISIBLE);
                placeRecyclerView.setVisibility(View.VISIBLE);

            } else if (!filteredCityList.isEmpty()) {
                cityRecyclerView.setVisibility(View.VISIBLE);
                placeRecyclerView.setVisibility(View.GONE);
            } else if (!filteredPlaceList.isEmpty()) {
                cityRecyclerView.setVisibility(View.GONE);
                placeRecyclerView.setVisibility(View.VISIBLE);
            } else {
                cityRecyclerView.setVisibility(View.GONE);
                placeRecyclerView.setVisibility(View.GONE);
            }
        }

        cityAdapter.notifyDataSetChanged();
        placeAdapter.notifyDataSetChanged();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Intent intent = null;

                    if (item.getItemId() == R.id.navigation_home) {
                        intent = new Intent(CityActivity.this, HomePage.class);
                    } else if (item.getItemId() == R.id.navigation_trips) {
                        intent = new Intent(CityActivity.this, TripActivity.class);
                    } else if (item.getItemId() == R.id.navigation_cities) {
                        intent = new Intent(CityActivity.this, CityActivity.class);
                    } else if (item.getItemId() == R.id.navigation_favorites) {
                        intent = new Intent(CityActivity.this, FavoritePage.class);
                    } else if (item.getItemId() == R.id.navigation_profile) {
                        intent = new Intent(CityActivity.this, ProfileActivity.class);
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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
         if (requestCode == EDITCity_REQUEST_CODE&& resultCode == RESULT_OK) {
            if (data != null && data.getBooleanExtra("CityEdited", false)) {
               fetchCitiesAndPlaces();

            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchCitiesAndPlaces(); // Refresh cities and places when activity is resumed
    }



}
