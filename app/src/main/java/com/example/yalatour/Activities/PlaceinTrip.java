package com.example.yalatour.Activities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yalatour.Adapters.PlaceinTripAdapter;
import com.example.yalatour.Classes.TourismPlaceClass;
import com.example.yalatour.Classes.TripClass;
import com.example.yalatour.DetailsActivity.TripDetails;
import com.example.yalatour.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class PlaceinTrip extends AppCompatActivity {

    private Button AddPlacetoTrip,CreateTripDialog;
    private RecyclerView TripPlace;
    private PlaceinTripAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private List<TripClass> Trips;
    private List<TourismPlaceClass> NewTripPlaces;
    private String PlaceId;
    private FirebaseUser currentUser;
    private  String currentuserId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_placein_trip);

        PlaceId=getIntent().getExtras().getString("PlaceId");
        db = FirebaseFirestore.getInstance();
        mAuth=FirebaseAuth.getInstance();
        TripPlace=findViewById(R.id.Tripplace);
        Trips=new ArrayList<>();
        adapter=new PlaceinTripAdapter(this,Trips);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(PlaceinTrip.this, 1);
        TripPlace.setLayoutManager(gridLayoutManager);
        TripPlace.setAdapter(adapter);
        currentUser = mAuth.getCurrentUser();
        currentuserId = currentUser.getUid();
        AddPlacetoTrip=findViewById(R.id.AddPlacetoTrip);
        CreateTripDialog=findViewById(R.id.CreateTripDialog);
        NewTripPlaces=new ArrayList<>();
        AddPlacetoTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                     addPlaceToSelectedTrips();

            }
        });
        CreateTripDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                   showAddTripDialog();
            }
        });
        fetchMyTrips();
    }
    private void addPlaceToSelectedTrips() {
        // Query Firestore to get the tourism place document
        db.collection("TourismPlaces").document(PlaceId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Convert the document snapshot to a TourismPlaceClass object
                        TourismPlaceClass place = documentSnapshot.toObject(TourismPlaceClass.class);
                        List<TripClass> selectedTrips = adapter.getSelectedTrips();
                        for (TripClass trip : selectedTrips) {
                            List<TourismPlaceClass> Allplaces = trip.getTripPlaces();
                            if (Allplaces == null) {
                                Allplaces = new ArrayList<>();
                            }
                            if (Allplaces.contains(place)) {
                                Toast.makeText(PlaceinTrip.this, "This place is already in your trip " + trip.getTripName(), Toast.LENGTH_SHORT).show();
                            } else {
                                Allplaces.add(place);


                                db.collection("Trips").document(trip.getTripId())
                                        .update("tripPlaces", Allplaces)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(PlaceinTrip.this, "This place is added to your trip" + trip.getTripName() + " successfully", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(PlaceinTrip.this, "Failed to add this place your trip ", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }

                    } else {
                        // Handle case where the place document does not exist
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle any errors that occurred during the retrieval
                });
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
                    Toast.makeText(PlaceinTrip.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    try {
                        int tripDays = Integer.parseInt(tripDaysString);
                        db.collection("TourismPlaces").document(PlaceId)
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        // Convert the document snapshot to a TourismPlaceClass object
                                        TourismPlaceClass place = documentSnapshot.toObject(TourismPlaceClass.class);
                                        NewTripPlaces.add(place);

                                        TripClass trip = new TripClass(null, tripDate, tripDays, null, NewTripPlaces, null, null, adminUserId, tripCode, tripName);

                                        db.collection("Trips").add(trip)
                                                .addOnSuccessListener(documentReference -> {
                                                    String tripId = documentReference.getId();
                                                    trip.setTripId(tripId);
                                                    documentReference.set(trip)
                                                            .addOnSuccessListener(aVoid -> {
                                                                Toast.makeText(PlaceinTrip.this, "A new Trip is Created and"+place.getPlaceName()+"is Added to it", Toast.LENGTH_SHORT).show();
                                                                fetchMyTrips();
                                                                nameEditText.setText("");
                                                                dateEditText.setText("");
                                                                nbDaysEditText.setText("");
                                                                dialog.dismiss();
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                Toast.makeText(PlaceinTrip.this, "Failed to Create New Trip", Toast.LENGTH_SHORT).show();
                                                            });
                                                });
                                    }
                                });
                    } catch (NumberFormatException e) {
                        Toast.makeText(PlaceinTrip.this, "Invalid trip days", Toast.LENGTH_SHORT).show();
                    }
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
                        // Handle errors
                        return;
                    }
                    if (value != null) {
                        Trips.clear();
                        for (DocumentSnapshot document : value.getDocuments()) {
                            TripClass tripClass = document.toObject(TripClass.class);

                            // Calculate the end date of the trip
                            Date endDate = tripClass.getEndDate2();

                            // Check if the trip is still valid (end date is in the future)
                            if (endDate != null && endDate.after(new Date())) {
                                Trips.add(tripClass);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

}
