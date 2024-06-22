package com.example.yalatour.Activities;

import static android.content.ContentValues.TAG;

import android.app.Dialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yalatour.Adapters.TripAdapter;
import com.example.yalatour.Classes.TripClass;
import com.example.yalatour.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class TripActivity extends AppCompatActivity {

    private RecyclerView tripRecyclerView;
    private FloatingActionButton addTripButton;
    private List<TripClass> tripList;
    private List<TripClass> filteredTripList;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private TripAdapter adapter;
    private SearchView SearchTrip;
    private String currentCategory = "MyTrips";

    private String currentuserId;
    private boolean isFirstLoad = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip);

        tripRecyclerView = findViewById(R.id.TriprecyclerView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(TripActivity.this, 1);
        tripRecyclerView.setLayoutManager(gridLayoutManager);
        tripList = new ArrayList<>();
        filteredTripList = new ArrayList<>();
        adapter = new TripAdapter(TripActivity.this, filteredTripList);
        tripRecyclerView.setAdapter(adapter);
        addTripButton = findViewById(R.id.AddTrip);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        currentuserId = currentUser.getUid();
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        bottomNav.getMenu().setGroupCheckable(0, true, false);
        for (int i = 0; i < bottomNav.getMenu().size(); i++) {
            bottomNav.getMenu().getItem(i).setChecked(false);
        }

        if (isFirstLoad) {
            fetchMyTrips();
            isFirstLoad = false;
        }
        addTripButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddTripDialog();
            }
        });

        fetchMyTrips();

        SearchTrip = findViewById(R.id.SearchTrip);
        SearchTrip.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterTrips(newText);
                return true;
            }
        });

    }

    private void filterTrips(String query) {
        filteredTripList.clear();
        if (query.isEmpty()) {
            filteredTripList.addAll(tripList);
        } else {
            for (TripClass trip : tripList) {
                if (trip.getTripName().toLowerCase().contains(query.toLowerCase())) {
                    filteredTripList.add(trip);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void showAddTripDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_add_trip, null);

        EditText nameEditText = dialogView.findViewById(R.id.Name);
        EditText dateEditText = dialogView.findViewById(R.id.Date);
        EditText nbDaysEditText = dialogView.findViewById(R.id.NbDays);

        Button saveButton = dialogView.findViewById(R.id.save_button);

        dateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.requestFocus();
                showDatePicker(dateEditText);
            }
        });

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(dialogView);

        // Set dialog window position to the bottom
        dialog.getWindow().setGravity(Gravity.BOTTOM);

        // Set dialog window width to match parent
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(dialog.getWindow().getAttributes());
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setAttributes(layoutParams);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tripName = nameEditText.getText().toString();
                String tripDate = dateEditText.getText().toString();
                String tripDaysString = nbDaysEditText.getText().toString();
                String tripCode = UUID.randomUUID().toString();
                String adminUserId = currentUser.getUid();

                if (tripName.isEmpty() || tripDate.isEmpty() || tripDaysString.isEmpty() || tripCode.isEmpty()) {
                    Toast.makeText(TripActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    int tripDays = Integer.parseInt(tripDaysString);
                    TripClass trip = new TripClass(null, tripDate, tripDays, null, null, null, null, adminUserId, tripCode, tripName);

                    db.collection("Trips").add(trip)
                            .addOnSuccessListener(documentReference -> {
                                String tripId = documentReference.getId();
                                trip.setTripId(tripId);
                                documentReference.set(trip)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(TripActivity.this, "A new Trip is Created", Toast.LENGTH_SHORT).show();
                                            if (adminUserId.equals(currentuserId)) {
                                                fetchMyTrips(); // Fetch trips created by the current user (admin)
                                            }
                                            if (currentCategory.equals("MyTrips")) {
                                                fetchMyTrips();
                                            } else if (currentCategory.equals("JoinedTrips")) {
                                                fetchJoinedTrips();
                                            } else if (currentCategory.equals("RequestedTrips")) {
                                                fetchRequestedTrips();
                                            }
                                            else if (currentCategory.equals("OldTrips")) {
                                                fetchOldTrips();
                                            }
                                            else{
                                                fetchAllTrips();
                                                fetchTripsNotMine();
                                            }

                                            nameEditText.setText("");
                                            dateEditText.setText("");
                                            nbDaysEditText.setText("");
                                            dialog.dismiss();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(TripActivity.this, "Failed to Create New Trip", Toast.LENGTH_SHORT).show();
                                        });
                            });
                }
            }
        });


        dialog.show();
    }


    private void showDatePicker(final EditText tripDateEditText) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        String date = (monthOfYear + 1) + "-" + dayOfMonth + "-" + year;
                        tripDateEditText.setText(date);
                    }
                }, year, month, day);

        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
        datePickerDialog.show();
    }

    public void fetchMyTrips() {
        db.collection("Trips")
                .whereEqualTo("tripAdminid", currentuserId)
                .orderBy("tripDate")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "fetchMyTrips: Error fetching joined trips", error);
                        return;
                    }
                    if (value != null) {
                        tripList.clear();
                        for (DocumentSnapshot document : value.getDocuments()) {
                            TripClass tripClass = document.toObject(TripClass.class);
                            if (tripClass != null && !isOldTrip(tripClass))
                                tripList.add(tripClass);
                        }
                        filterTrips(SearchTrip.getQuery().toString()); // Apply filter after fetching data
                    }
                });
    }

    public void fetchJoinedTrips() {
        db.collection("Trips")
                .whereArrayContains("usersid", currentuserId)
                .orderBy("tripDate")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "fetchJoinedTrips: Error fetching joined trips", error);
                        return;
                    }
                    if (value != null) {
                        tripList.clear();
                        for (DocumentSnapshot document : value.getDocuments()) {
                            TripClass tripClass = document.toObject(TripClass.class);
                            if (tripClass != null && !isOldTrip(tripClass))
                                tripList.add(tripClass);
                        }
                        filterTrips(SearchTrip.getQuery().toString());
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    public void fetchAllTrips() {
        db.collection("Trips")
                .orderBy("tripDate")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "fetchAllTrips: Error fetching trips", error);
                        return;
                    }
                    if (value != null) {
                        tripList.clear();
                        for (DocumentSnapshot document : value.getDocuments()) {
                            TripClass tripClass = document.toObject(TripClass.class);
                            if (tripClass != null && !isOldTrip(tripClass)) {
                                List<String> usersid = tripClass.getUsersid();
                                if (usersid == null || !usersid.contains(currentuserId)) {
                                    tripList.add(tripClass);
                                }
                            }
                        }
                        filterTrips(SearchTrip.getQuery().toString()); // Apply filter after fetching data
                    }
                });
    }


    public void fetchTripsNotMine() {
        db.collection("Trips")
                .whereNotEqualTo("tripAdminid", currentuserId)
                .orderBy("tripAdminid")
                .orderBy("tripDate")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "fetchTripsNotMine: Error fetching joined trips", error);
                        return;
                    }
                    if (value != null) {
                        tripList.clear();
                        for (DocumentSnapshot document : value.getDocuments()) {
                            TripClass tripClass = document.toObject(TripClass.class);
                            if (tripClass != null && !isOldTrip(tripClass))
                                tripList.add(tripClass);
                        }
                        filterTrips(SearchTrip.getQuery().toString()); // Apply filter after fetching data
                    }
                });
    }

    public void fetchOldTrips() {
        db.collection("Trips")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "fetchOldTrips: Error fetching trips", error);
                        return;
                    }
                    if (value != null) {
                        tripList.clear();
                        for (DocumentSnapshot document : value.getDocuments()) {
                            TripClass tripClass = document.toObject(TripClass.class);
                            if (tripClass != null && isOldTrip(tripClass) && (tripClass.getTripAdminid().equals(currentuserId) || tripClass.getUsersid().contains(currentuserId))) {
                                tripList.add(tripClass);
                            }
                        }
                        filterTrips(SearchTrip.getQuery().toString());
                        Log.d(TAG, "fetchRequestedTrips: Old trips fetched successfully. Count: " + tripList.size());
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private boolean isOldTrip(TripClass trip) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault());
            Calendar tripEndDate = Calendar.getInstance();
            tripEndDate.setTime(sdf.parse(trip.getTripDate()));

            int numberOfDays = trip.getNumberofDays() == 0 ? 1 : trip.getNumberofDays();
            tripEndDate.add(Calendar.DAY_OF_YEAR, numberOfDays);

            Calendar currentDate = Calendar.getInstance();
            return tripEndDate.before(currentDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }



    public void fetchRequestedTrips() {
        tripList.clear(); // Clear the list before fetching new data
        adapter.notifyDataSetChanged();

        db.collection("Trips")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        // Handle errors
                        Log.e(TAG, "fetchRequestedTrips: Error fetching requested trips", error);
                        return;
                    }
                    if (value != null) {
                        tripList.clear();
                        for (DocumentSnapshot document : value.getDocuments()) {
                            TripClass tripClass = document.toObject(TripClass.class);
                            if (tripClass != null && !isOldTrip(tripClass)) {
                                List<Map<String, Object>> requests = (List<Map<String, Object>>) document.get("requests");
                                if (requests != null) {
                                    for (Map<String, Object> request : requests) {
                                        String requestUserId = (String) request.get("request_UserId");
                                        if (currentuserId.equals(requestUserId)) {
                                            tripList.add(tripClass);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        filterTrips(SearchTrip.getQuery().toString()); // Apply filter after fetching data
                        Log.d(TAG, "fetchRequestedTrips: Requested trips fetched successfully. Count: " + tripList.size());
                    }
                });
    }

    public void onCategoryClick(View view) {
        tripList.clear();
        adapter.notifyDataSetChanged();
        if (view.getId() == R.id.MyTrips) {
            currentCategory = "MyTrips";
            fetchMyTrips();
            addTripButton.setVisibility(View.VISIBLE);
        } else if (view.getId() == R.id.JoinedTrips) {
            currentCategory = "JoinedTrips";
            fetchJoinedTrips();
            addTripButton.setVisibility(View.GONE);
        } else if (view.getId() == R.id.RequestedTrips) {
            currentCategory = "RequestedTrips";
            fetchRequestedTrips();
            addTripButton.setVisibility(View.GONE);
        } else if (view.getId() == R.id.AllTrips) {
            currentCategory = "AllTrips";
            fetchAllTrips();
            fetchTripsNotMine();
            addTripButton.setVisibility(View.GONE);
        }
        else if (view.getId() == R.id.OldTrips) {
            currentCategory = "OldTrips";
            fetchOldTrips();
            addTripButton.setVisibility(View.GONE);
        }
    }
    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Intent intent = null;

                    if (item.getItemId() == R.id.navigation_home) {
                        intent = new Intent(TripActivity.this, HomePage.class);
                    } else if (item.getItemId() == R.id.navigation_trips) {
                        intent = new Intent(TripActivity.this, TripActivity.class);
                    } else if (item.getItemId() == R.id.navigation_cities) {
                        intent = new Intent(TripActivity.this, CityActivity.class);
                    } else if (item.getItemId() == R.id.navigation_favorites) {
                        intent = new Intent(TripActivity.this, FavoritePage.class);
                    } else if (item.getItemId() == R.id.navigation_profile) {
                        intent = new Intent(TripActivity.this, ProfileActivity.class);
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
