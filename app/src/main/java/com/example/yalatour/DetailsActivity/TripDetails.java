package com.example.yalatour.DetailsActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yalatour.Activities.SelectingPlacesActivity;
import com.example.yalatour.Adapters.MembersAdapter;
import com.example.yalatour.Adapters.RequestsAdapter;
import com.example.yalatour.Adapters.RequirementsAdapter;
import com.example.yalatour.Adapters.TourismPlaceAdapter;
import com.example.yalatour.Classes.MyRequirementsClass;
import com.example.yalatour.Classes.TripClass;
import com.example.yalatour.Classes.TripRequestsClass;
import com.example.yalatour.Classes.TripRequirementsClass;
import com.example.yalatour.Classes.TourismPlaceClass;
import com.example.yalatour.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class TripDetails extends AppCompatActivity {

    private static final String TAG = "TripDetails";

    TextView AddPlace;
    RecyclerView RequirementsRecyclerView;
    RequirementsAdapter requirementsAdapter;
    List<TripRequirementsClass> requirementsList;
    RecyclerView MyRequirementsRecyclerView;
    List<TripRequirementsClass> MyrequirementsList;
    RecyclerView PlaceRecyclerView;
    TourismPlaceAdapter PlaceAdapter;
    RequestsAdapter RequestAdapter;
    RecyclerView RequetsRecyclerView;
    List<TripRequestsClass> RequestsList;
    List<TourismPlaceClass> PlacesList;
    RecyclerView MembersRecyclerView;
    MembersAdapter MembersAdapter;
    List<String> Membersids;
    String currentuserId;
    FirebaseUser currentUser;

    FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private boolean isUserAdmin;

    String TripId;
    Button Save;
    Button SaveMyRequirements;

    boolean viewRequests;

    private static final int Places_REQUEST_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        currentuserId = currentUser.getUid();
        TripId = getIntent().getStringExtra("TripId");
        Log.d("TripDetails", "Trip ID:" + TripId);

        if (TripId == null) {
            Toast.makeText(this, "Error: Trip ID is missing.", Toast.LENGTH_LONG).show();
            finish(); // Close the activity if TripId is missing
            return;
        }

        AddPlace = findViewById(R.id.Addplace);
        RequirementsRecyclerView = findViewById(R.id.RequirementsRecyclerView);
        requirementsList = new ArrayList<>();
        MyrequirementsList = new ArrayList<>();
        RequestsList = new ArrayList<>();
        PlacesList = new ArrayList<>();
        Membersids = new ArrayList<>();

        // Initialize RequestsRecyclerView adapter
        RequetsRecyclerView = findViewById(R.id.RequestsRecyclerView);
        RequestAdapter = new RequestsAdapter(this, RequestsList, TripId);
        RequetsRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        RequetsRecyclerView.setAdapter(RequestAdapter);

        // Initialize MembersRecyclerView adapter
        MembersRecyclerView = findViewById(R.id.MembersRecyclerView);
        MembersAdapter = new MembersAdapter(this, Membersids, TripId);
        MembersRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        MembersRecyclerView.setAdapter(MembersAdapter);

        // Initialize PlaceRecyclerView adapter
        PlaceRecyclerView = findViewById(R.id.PlacerecyclerView);
        PlaceRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        PlaceAdapter = new TourismPlaceAdapter(TripDetails.this, PlacesList);
        PlaceRecyclerView.setAdapter(PlaceAdapter);

        Save = findViewById(R.id.Save);
        SaveMyRequirements = findViewById(R.id.SaveMyRequirements);
        Save.setVisibility(View.GONE);

        AddPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TripDetails.this, SelectingPlacesActivity.class);
                intent.putExtra("TripId", TripId);
                startActivityForResult(intent, Places_REQUEST_CODE);
            }
        });

        Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveRequirements();
            }
        });

        SaveMyRequirements.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveMyRequirements();
            }
        });

        checkAdminAndInitialize();
        fetchSelectedPlaces();
        fetchRequirements();
    }

    private void checkAdminAndInitialize() {
        db.collection("Trips").document(TripId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String tripAdminId = documentSnapshot.toObject(TripClass.class).getTripAdminid();
                        if (currentUser != null && currentUser.getUid().equals(tripAdminId)) {
                            isUserAdmin = true;
                        } else {
                            isUserAdmin = false;
                        }
                        initializeRequirementsRecyclerView();
                        updateUI();
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                });
    }

    private void initializeRequirementsRecyclerView() {
        requirementsAdapter = new RequirementsAdapter(this, requirementsList, isUserAdmin);
        RequirementsRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        RequirementsRecyclerView.setAdapter(requirementsAdapter);
        MyRequirementsRecyclerView = findViewById(R.id.MyRequirementsRecyclerView);
        requirementsAdapter = new RequirementsAdapter(this, MyrequirementsList, true);
        MyRequirementsRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        MyRequirementsRecyclerView.setAdapter(requirementsAdapter);
        fetchMyRequirements();
        fetchRequirements();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Places_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null && data.getBooleanExtra("PlacesAdded", false)) {
                fetchSelectedPlaces();
            }
        }
    }

    private void fetchSelectedPlaces() {
        db.collection("Trips").document(TripId)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) {
                        return;
                    }
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        List<TourismPlaceClass> updatedPlaces = documentSnapshot.toObject(TripClass.class).getTripPlaces();
                        PlacesList.clear();
                        if (updatedPlaces != null) {
                            PlacesList.addAll(updatedPlaces);
                        }
                        PlaceAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void fetchMyRequirements() {
        db.collection("MyRequirements")
                .whereEqualTo("tripid", TripId)
                .whereEqualTo("requrement_Userid", currentuserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            MyRequirementsClass myRequirements = document.toObject(MyRequirementsClass.class);
                            if (myRequirements != null) {
                                List<TripRequirementsClass> myRequirementsList = myRequirements.getRequirements();
                                MyrequirementsList.clear();
                                if (myRequirementsList != null && !myRequirementsList.isEmpty()) {
                                    MyrequirementsList.addAll(myRequirementsList);
                                } else {
                                    MyrequirementsList.add(new TripRequirementsClass("", false));
                                }
                                requirementsAdapter.notifyDataSetChanged();
                            }
                        }
                    } else {
                        MyrequirementsList.clear();
                        MyrequirementsList.add(new TripRequirementsClass("", false));
                        requirementsAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                });
    }

    private void fetchMembers() {
        db.collection("Trips").document(TripId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> membersIdsList = documentSnapshot.toObject(TripClass.class).getUsersid();
                        Membersids.clear();
                        if (membersIdsList != null && !membersIdsList.isEmpty()) {
                            Membersids.addAll(membersIdsList);
                        }
                        MembersAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                });
    }

    private void fetchRequests() {
        db.collection("Trips").document(TripId)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) {
                        return;
                    }
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        List<TripRequestsClass> updatedRequests = documentSnapshot.toObject(TripClass.class).getRequests();
                        RequestsList.clear();
                        if (updatedRequests != null) {
                            RequestsList.addAll(updatedRequests);
                        }

                        RequestAdapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchRequirements();
    }

    private void fetchRequirements() {
        db.collection("Trips").document(TripId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<TripRequirementsClass> updatedRequirements = documentSnapshot.toObject(TripClass.class).getRequirements();
                        requirementsList.clear();
                        if (updatedRequirements != null) {
                            requirementsList.addAll(updatedRequirements);
                        }
                        else {
                            requirementsList.add(new TripRequirementsClass("", false));
                        }
                        requirementsAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                });
    }

    private void saveRequirements() {
        db.collection("Trips").document(TripId)
                .update("requirements", requirementsList)
                .addOnSuccessListener(aVoid -> {
                    fetchRequirements();
                    requirementsAdapter.notifyDataSetChanged();
                    Toast.makeText(TripDetails.this, "Requirements saved successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(TripDetails.this, "Failed to save requirements", Toast.LENGTH_SHORT).show();
                });
    }


    private void saveMyRequirements() {
        MyRequirementsClass myRequirement = new MyRequirementsClass(null, TripId, currentuserId, MyrequirementsList);

        db.collection("MyRequirements")
                .whereEqualTo("tripid", TripId)
                .whereEqualTo("requrement_Userid", currentuserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                        String myRequirementId = documentSnapshot.getId();
                        myRequirement.setMyRequirementsId(myRequirementId);
                        db.collection("MyRequirements").document(myRequirementId)
                                .set(myRequirement)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(TripDetails.this, "My Requirements updated successfully", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(TripDetails.this, "Failed to update My Requirements", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        db.collection("MyRequirements")
                                .add(myRequirement)
                                .addOnSuccessListener(documentReference -> {
                                    Toast.makeText(TripDetails.this, "My Requirements saved successfully", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(TripDetails.this, "Failed to save My Requirements", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(TripDetails.this, "Failed to fetch My Requirements", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUI() {
        if (isUserAdmin) {
            // Show UI elements for admin
            MembersRecyclerView.setVisibility(View.VISIBLE);
            AddPlace.setVisibility(View.VISIBLE);
            RequetsRecyclerView.setVisibility(View.VISIBLE);
            findViewById(R.id.Members).setVisibility(View.VISIBLE);
            findViewById(R.id.Requests).setVisibility(View.VISIBLE);
        } else {
            // Hide UI elements for non-admin users
            MembersRecyclerView.setVisibility(View.GONE);
            AddPlace.setVisibility(View.GONE);
            RequetsRecyclerView.setVisibility(View.GONE);
            findViewById(R.id.Members).setVisibility(View.GONE);
            findViewById(R.id.Requests).setVisibility(View.GONE);
        }
    }
    public void onCategoryClick(View view) {
        // Handle click events for different cateagories
        if (view.getId() == R.id.Places) {
            // Show places and hide requirements and requests
            PlaceRecyclerView.setVisibility(View.VISIBLE);
            RequirementsRecyclerView.setVisibility(View.GONE);
            Save.setVisibility(View.GONE);
            RequetsRecyclerView.setVisibility(View.GONE);
            MembersRecyclerView.setVisibility(View.GONE);
            SaveMyRequirements.setVisibility(View.GONE);
            MyRequirementsRecyclerView.setVisibility(View.GONE);
            if (isUserAdmin) {
                AddPlace.setVisibility(View.VISIBLE);
            } else {
                AddPlace.setVisibility(View.GONE);
            }
        } else if (view.getId() == R.id.Requirements) {
            Log.d("TripDetails", "isUserAdmin on Requirements:"+isUserAdmin);
            RequirementsRecyclerView.setVisibility(View.VISIBLE);
            fetchRequirements();
            PlaceRecyclerView.setVisibility(View.GONE);
            AddPlace.setVisibility(View.GONE);
            Save.setVisibility(View.VISIBLE);
            RequetsRecyclerView.setVisibility(View.GONE);
            MembersRecyclerView.setVisibility(View.GONE);
            SaveMyRequirements.setVisibility(View.GONE);
            MyRequirementsRecyclerView.setVisibility(View.GONE);
        }
        else if (view.getId() == R.id.Members) {
            RequirementsRecyclerView.setVisibility(View.GONE);
            PlaceRecyclerView.setVisibility(View.GONE);
            SaveMyRequirements.setVisibility(View.GONE);
            MyRequirementsRecyclerView.setVisibility(View.GONE);
            AddPlace.setVisibility(View.GONE);
            Save.setVisibility(View.GONE);
            MembersRecyclerView.setVisibility(View.VISIBLE);
            RequetsRecyclerView.setVisibility(View.GONE);
            fetchMembers();
        } else if (view.getId() == R.id.Requests) {
            RequirementsRecyclerView.setVisibility(View.GONE);
            PlaceRecyclerView.setVisibility(View.GONE);
            AddPlace.setVisibility(View.GONE);
            Save.setVisibility(View.GONE);
            MembersRecyclerView.setVisibility(View.GONE);
            SaveMyRequirements.setVisibility(View.GONE);
            MyRequirementsRecyclerView.setVisibility(View.GONE);
            RequetsRecyclerView.setVisibility(View.VISIBLE);
            fetchRequests();
        }
        else if (view.getId() == R.id.MyRequirements) {
            // Show the RecyclerView for requirements
            RequirementsRecyclerView.setVisibility(View.GONE);
            SaveMyRequirements.setVisibility(View.VISIBLE);
            MyRequirementsRecyclerView.setVisibility(View.VISIBLE);
            PlaceRecyclerView.setVisibility(View.GONE);
            AddPlace.setVisibility(View.GONE);
            Save.setVisibility(View.GONE);
            RequetsRecyclerView.setVisibility(View.GONE);
            MembersRecyclerView.setVisibility(View.GONE);
            fetchMyRequirements();
        }



    }


}
