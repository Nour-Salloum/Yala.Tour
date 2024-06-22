package com.example.yalatour.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectingPlacesActivity extends AppCompatActivity {

    RecyclerView selectingPlacesRecyclerView;
    List<TourismPlaceClass> allPlacesList;
    List<TourismPlaceClass> filteredPlaceList;
    FirebaseFirestore db;
    TextView done;
    String TripId;
    SelectingPlacesAdapter adapter;
    List<TourismPlaceClass> selectedPlaces;
    SearchView SelectingPlacesSearchView;
    ImageButton BackButton;
    private Map<String, String> cityIdToNameMap; // Map to store city names

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selecting_places);

        selectingPlacesRecyclerView = findViewById(R.id.SlectingPlacesrecyclerView);
        selectingPlacesRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        SelectingPlacesSearchView = findViewById(R.id.SelectingPlacesearchView);
        BackButton = findViewById(R.id.BackButton);
        db = FirebaseFirestore.getInstance();
        TripId = getIntent().getStringExtra("TripId");
        Log.d("Activity", "TripId: " + TripId);
        allPlacesList = new ArrayList<>();
        done = findViewById(R.id.DoneAddingPlaces);
        selectedPlaces = new ArrayList<>();
        filteredPlaceList = new ArrayList<>();


        fetchSelectedPlaces();

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.collection("Trips").document(TripId)
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

        BackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        fetchCityNames();
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

    private void fetchCityNames() {
        cityIdToNameMap = new HashMap<>();
        db.collection("Cities")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            String cityId = document.getId();
                            String cityTitle = document.getString("cityTitle");
                            cityIdToNameMap.put(cityId, cityTitle);

                        }
                        fetchAllPlaces();
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
        if (TripId != null) {
            db.collection("Trips").document(TripId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Convert the document snapshot to TripClass (assuming TripClass has been defined)
                            TripClass trip = documentSnapshot.toObject(TripClass.class);

                            if (trip != null) {
                                selectedPlaces = trip.getTripPlaces();
                                if (selectedPlaces != null && !selectedPlaces.isEmpty()) {
                                    // Update the adapter with selected places
                                    adapter = new SelectingPlacesAdapter(this, filteredPlaceList, selectedPlaces);
                                    selectingPlacesRecyclerView.setAdapter(adapter);
                                    adapter.notifyDataSetChanged();
                                    Log.d("Activity", "Selected places from Firestore: " + selectedPlaces.toString());
                                } else {
                                    adapter = new SelectingPlacesAdapter(SelectingPlacesActivity.this, filteredPlaceList, new ArrayList<>());
                                    selectingPlacesRecyclerView.setAdapter(adapter);
                                }
                            } else {
                                Log.d("SelectedPlaces", "TripClass is null");
                            }
                        } else {
                            Log.d("SelectedPlaces", "Document does not exist");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Activity", "Failed to fetch selected places: " + e.getMessage());
                    });
        } else {
            Log.d("SelectedPlaces", "TripId is null");
        }
    }


    public void filterPlaces(String query) {
        filteredPlaceList.clear();
        String lowerCaseQuery = query.toLowerCase();


        for (TourismPlaceClass place : allPlacesList) {
            String cityTitle = cityIdToNameMap.get(place.getCityId());

            if (place.getPlaceName().toLowerCase().contains(lowerCaseQuery) ||
                    (cityTitle != null && cityTitle.toLowerCase().contains(lowerCaseQuery))) {
                filteredPlaceList.add(place);

            }
        }

        adapter.notifyDataSetChanged();
    }
}
