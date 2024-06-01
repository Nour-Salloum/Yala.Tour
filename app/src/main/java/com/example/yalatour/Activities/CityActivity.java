package com.example.yalatour.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.yalatour.Adapters.MyAdapter;
import com.example.yalatour.Adapters.TourismPlaceAdapter;
import com.example.yalatour.Classes.CityClass;
import com.example.yalatour.Classes.TourismPlaceClass;
import com.example.yalatour.R;
import com.example.yalatour.UploadActivities.UploadActivity;
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
        placeRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));

        cityRecyclerView.setAdapter(cityAdapter);
        placeRecyclerView.setAdapter(placeAdapter);

        setupFab();
        fetchCitiesAndPlaces();

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
        db.collection("Cities")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        cityList.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            CityClass city = document.toObject(CityClass.class);
                            cityList.add(city);
                        }
                        filteredCityList.addAll(cityList);
                        cityAdapter.notifyDataSetChanged();
                    }
                });

        db.collection("TourismPlaces")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        placeList.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            TourismPlaceClass place = document.toObject(TourismPlaceClass.class);
                            placeList.add(place);
                        }
                        filteredPlaceList.addAll(placeList);
                        placeAdapter.notifyDataSetChanged();
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

            if (!filteredPlaceList.isEmpty()) {
                placeRecyclerView.setVisibility(View.VISIBLE);
                cityRecyclerView.setVisibility(View.GONE);
            } else {
                placeRecyclerView.setVisibility(View.GONE);
                cityRecyclerView.setVisibility(View.VISIBLE);
            }
        }

        cityAdapter.notifyDataSetChanged();
        placeAdapter.notifyDataSetChanged();
    }

}
