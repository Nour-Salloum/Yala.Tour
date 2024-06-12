package com.example.yalatour.DetailsActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yalatour.Activities.CityActivity;
import com.example.yalatour.Activities.FavoriteActivity;
import com.example.yalatour.Activities.HomePage;
import com.example.yalatour.Activities.ProfileActivity;
import com.example.yalatour.Activities.SelectingPlacesActivity;
import com.example.yalatour.Activities.TripActivity;
import com.example.yalatour.Adapters.MembersAdapter;
import com.example.yalatour.Adapters.MemoryImageAdapter;
import com.example.yalatour.Adapters.MemoryTextAdapter;
import com.example.yalatour.Adapters.MemoryVideoAdapter;
import com.example.yalatour.Adapters.RequestsAdapter;
import com.example.yalatour.Adapters.RequirementsAdapter;
import com.example.yalatour.Adapters.TourismPlaceAdapter;
import com.example.yalatour.Classes.MemoriesClass;
import com.example.yalatour.Classes.MyRequirementsClass;
import com.example.yalatour.Classes.TripClass;
import com.example.yalatour.Classes.TripRequestsClass;
import com.example.yalatour.Classes.TripRequirementsClass;
import com.example.yalatour.Classes.TourismPlaceClass;
import com.example.yalatour.EditActivities.EditMemoryActivity;
import com.example.yalatour.R;
import com.example.yalatour.UploadActivities.UploadMemoryActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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


    MemoryImageAdapter memoryImageAdapter;
    MemoryTextAdapter memoryTextAdapter;
    MemoryVideoAdapter memoryVideoAdapter;
    RecyclerView memoriesImageRecyclerView;
    RecyclerView memoriesTextRecyclerView;
    RecyclerView memoriesVideoRecyclerView;

    List<String> imageUrls;
    List<String> videoUrls;
    List<String> texts;
    List<MemoriesClass> memoriesList;
    String currentuserId;
    FirebaseUser currentUser;

    FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private boolean isUserAdmin;

    private FloatingActionButton AddMemory;
    private FloatingActionButton EditMemory;

    private static final int UPLOAD_Memory_REQUEST_CODE = 12345;
    private static final int Edit_Memory_REQUEST_CODE = 123456;

    String TripId;
    Button Save;
    Button SaveMyRequirements;



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
        AddMemory = findViewById(R.id.AddMemory);
        EditMemory = findViewById(R.id.EditMemory);
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

        // Initialize lists
        imageUrls = new ArrayList<>();
        videoUrls = new ArrayList<>();
        texts = new ArrayList<>();
        memoriesList = new ArrayList<>();
        // Set adapters
        memoriesImageRecyclerView = findViewById(R.id.MemoriesImageRecyclerView);
        memoriesTextRecyclerView = findViewById(R.id.MemoriesTextRecyclerView);
        memoriesVideoRecyclerView = findViewById(R.id.MemoriesVideoRecyclerView);
        memoryImageAdapter = new MemoryImageAdapter(this, imageUrls);
        memoryVideoAdapter = new MemoryVideoAdapter(this, videoUrls);
        memoryTextAdapter = new MemoryTextAdapter(this, texts);

        memoriesImageRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        memoriesImageRecyclerView.setAdapter(memoryImageAdapter);

        memoriesVideoRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        memoriesVideoRecyclerView.setAdapter(memoryVideoAdapter);

        memoriesTextRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        memoriesTextRecyclerView.setAdapter(memoryTextAdapter);


        Save = findViewById(R.id.Save);
        SaveMyRequirements = findViewById(R.id.SaveMyRequirements);
        Save.setVisibility(View.GONE);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        bottomNav.getMenu().setGroupCheckable(0, true, false);
        for (int i = 0; i < bottomNav.getMenu().size(); i++) {
            bottomNav.getMenu().getItem(i).setChecked(false);
        }

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
        AddMemory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TripDetails.this, UploadMemoryActivity.class);
                intent.putExtra("tripId", TripId);
                startActivityForResult(intent, UPLOAD_Memory_REQUEST_CODE);
            }
        });
        EditMemory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TripDetails.this, EditMemoryActivity.class);
                intent.putExtra("MemoryId",getMemoryId());
                startActivityForResult(intent, UPLOAD_Memory_REQUEST_CODE);
            }
        });
        if(getMemoryId()!=null){
            EditMemory.setVisibility(View.VISIBLE);
            AddMemory.setVisibility(View.GONE);
        }
        else{
            EditMemory.setVisibility(View.GONE);
            AddMemory.setVisibility(View.VISIBLE);
        }

        checkAdminAndInitialize();
        checkIfTripIsOld();
        fetchSelectedPlaces();
        fetchRequirements();
        updateUI();

    }
    private String getMemoryId() {
        for (MemoriesClass memory : memoriesList) {
            if (memory.getMemory_UserId().equals(currentuserId) && memory.getMemory_TripId().equals(TripId)) {
                return memory.getMemoryId();
            }
        }
        return null;
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
        fetchMemories();
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
    private void fetchMemories() {
        db.collection("Memories")
                .whereEqualTo("memory_TripId", TripId)
                .whereEqualTo("memory_UserId", currentuserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    imageUrls.clear();
                    videoUrls.clear();
                    texts.clear();
                    memoriesList.clear();

                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        MemoriesClass memory = document.toObject(MemoriesClass.class);
                        if (memory != null) {
                            memoriesList.add(memory);
                            // Add images
                            List<String> images = memory.getMemory_Images();
                            if (images != null && !images.isEmpty()) {
                                imageUrls.addAll(images);
                            }

                            // Add videos
                            List<String> videos = memory.getMemory_Videos();
                            if (videos != null && !videos.isEmpty()) {
                                videoUrls.addAll(videos);
                            }

                            // Add texts
                            List<String> memoryTexts = memory.getMemory_Texts();
                            if (memoryTexts != null && !memoryTexts.isEmpty()) {
                                texts.addAll(memoryTexts); // Use a different variable name to avoid confusion
                            }
                        }
                    }

                    // Notify adapters of data changes
                    memoryImageAdapter.notifyDataSetChanged();
                    memoryVideoAdapter.notifyDataSetChanged();
                    memoryTextAdapter.notifyDataSetChanged();

                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    Log.e(TAG, "Error fetching memories: " + e.getMessage());
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
    private void checkIfTripIsOld() {
        db.collection("Trips").document(TripId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        TripClass trip = documentSnapshot.toObject(TripClass.class);
                        if (trip != null) {
                            String tripDateStr = trip.getTripDate();
                            int numberOfDays = trip.getNumberofDays();
                            if (numberOfDays == 0) {
                                numberOfDays = 1;
                            }

                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            try {
                                Date tripDate = sdf.parse(tripDateStr);
                                if (tripDate != null) {
                                    Calendar calendar = Calendar.getInstance();
                                    calendar.setTime(tripDate);
                                    calendar.add(Calendar.DAY_OF_YEAR, numberOfDays);

                                    Date endDate = calendar.getTime();
                                    Date currentDate = new Date();

                                    if (currentDate.after(endDate)) {
                                        hideOldTripButtons();
                                    }
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                });
    }

    private void hideOldTripButtons() {
        Save.setVisibility(View.GONE);
        SaveMyRequirements.setVisibility(View.GONE);
        AddPlace.setVisibility(View.GONE);
    }


    private void updateUI() {
        if (isUserAdmin) {
            // Show UI elements for admin
            MembersRecyclerView.setVisibility(View.VISIBLE);
            RequetsRecyclerView.setVisibility(View.VISIBLE);
            findViewById(R.id.Members).setVisibility(View.VISIBLE);
            findViewById(R.id.Requests).setVisibility(View.VISIBLE);
        } else {
            // Hide UI elements for non-admin users
            MembersRecyclerView.setVisibility(View.GONE);
            RequetsRecyclerView.setVisibility(View.GONE);
            findViewById(R.id.Members).setVisibility(View.GONE);
            findViewById(R.id.Requests).setVisibility(View.GONE);
        }



    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UPLOAD_Memory_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null && data.getBooleanExtra("MemoryAdded", false)) {
                fetchMemories();
            }
        }
        if (requestCode == Edit_Memory_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null && data.getBooleanExtra("MemoryEdited", false)) {
                fetchMemories();
            }
        }
    }
    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Intent intent = null;

                    if (item.getItemId() == R.id.navigation_home) {
                        intent = new Intent(TripDetails.this, HomePage.class);
                    } else if (item.getItemId() == R.id.navigation_trips) {
                        intent = new Intent(TripDetails.this, TripActivity.class);
                    } else if (item.getItemId() == R.id.navigation_cities) {
                        intent = new Intent(TripDetails.this, CityActivity.class);
                    } else if (item.getItemId() == R.id.navigation_favorites) {
                        intent = new Intent(TripDetails.this, FavoriteActivity.class);
                    } else if (item.getItemId() == R.id.navigation_profile) {
                        intent = new Intent(TripDetails.this, ProfileActivity.class);
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
            MembersRecyclerView.setVisibility(View.GONE);
            if(getMemoryId()!=null){
                EditMemory.setVisibility(View.VISIBLE);
                AddMemory.setVisibility(View.GONE);
            }
            else{
                EditMemory.setVisibility(View.GONE);
                AddMemory.setVisibility(View.VISIBLE);
            }
            memoriesTextRecyclerView.setVisibility(View.GONE);
            memoriesImageRecyclerView.setVisibility(View.GONE);
            memoriesVideoRecyclerView.setVisibility(View.GONE);
            if (isUserAdmin) {
                AddPlace.setVisibility(View.VISIBLE);
            } else {
                AddPlace.setVisibility(View.GONE);
            }
            checkIfTripIsOld();
        } else if (view.getId() == R.id.Requirements) {
            Log.d("TripDetails", "isUserAdmin on Requirements:"+isUserAdmin);
            RequirementsRecyclerView.setVisibility(View.VISIBLE);

            PlaceRecyclerView.setVisibility(View.GONE);
            AddPlace.setVisibility(View.GONE);
            Save.setVisibility(View.VISIBLE);
            RequetsRecyclerView.setVisibility(View.GONE);
            MembersRecyclerView.setVisibility(View.GONE);
            SaveMyRequirements.setVisibility(View.GONE);
            MyRequirementsRecyclerView.setVisibility(View.GONE);
            MembersRecyclerView.setVisibility(View.GONE);
            if(getMemoryId()!=null){
                EditMemory.setVisibility(View.VISIBLE);
                AddMemory.setVisibility(View.GONE);
            }
            else{
                EditMemory.setVisibility(View.GONE);
                AddMemory.setVisibility(View.VISIBLE);
            }
            memoriesTextRecyclerView.setVisibility(View.GONE);
            memoriesImageRecyclerView.setVisibility(View.GONE);
            memoriesVideoRecyclerView.setVisibility(View.GONE);
            fetchRequirements();
            checkIfTripIsOld();
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
            MembersRecyclerView.setVisibility(View.GONE);
            if(getMemoryId()!=null){
                EditMemory.setVisibility(View.VISIBLE);
                AddMemory.setVisibility(View.GONE);
            }
            else{
                EditMemory.setVisibility(View.GONE);
                AddMemory.setVisibility(View.VISIBLE);
            }
            memoriesTextRecyclerView.setVisibility(View.GONE);
            memoriesImageRecyclerView.setVisibility(View.GONE);
            memoriesVideoRecyclerView.setVisibility(View.GONE);
            fetchMembers();
            checkIfTripIsOld();
        } else if (view.getId() == R.id.Requests) {
            RequirementsRecyclerView.setVisibility(View.GONE);
            PlaceRecyclerView.setVisibility(View.GONE);
            AddPlace.setVisibility(View.GONE);
            Save.setVisibility(View.GONE);
            MembersRecyclerView.setVisibility(View.GONE);
            SaveMyRequirements.setVisibility(View.GONE);
            MyRequirementsRecyclerView.setVisibility(View.GONE);
            RequetsRecyclerView.setVisibility(View.VISIBLE);
            MembersRecyclerView.setVisibility(View.GONE);
            if(getMemoryId()!=null){
                EditMemory.setVisibility(View.VISIBLE);
                AddMemory.setVisibility(View.GONE);
            }
            else{
                EditMemory.setVisibility(View.GONE);
                AddMemory.setVisibility(View.VISIBLE);
            }
            memoriesTextRecyclerView.setVisibility(View.GONE);
            memoriesImageRecyclerView.setVisibility(View.GONE);
            memoriesVideoRecyclerView.setVisibility(View.GONE);
            fetchRequests();
            checkIfTripIsOld();
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
            MembersRecyclerView.setVisibility(View.GONE);
            if(getMemoryId()!=null){
                EditMemory.setVisibility(View.VISIBLE);
                AddMemory.setVisibility(View.GONE);
            }
            else{
                EditMemory.setVisibility(View.GONE);
                AddMemory.setVisibility(View.VISIBLE);
            }
            memoriesTextRecyclerView.setVisibility(View.GONE);
            memoriesImageRecyclerView.setVisibility(View.GONE);
            memoriesVideoRecyclerView.setVisibility(View.GONE);
            fetchMyRequirements();
            checkIfTripIsOld();
        }
        else if (view.getId() == R.id.Memories) {
            memoriesTextRecyclerView.setVisibility(View.VISIBLE);
            memoriesImageRecyclerView.setVisibility(View.VISIBLE);
            memoriesVideoRecyclerView.setVisibility(View.VISIBLE);
            if(getMemoryId()!=null){
                EditMemory.setVisibility(View.VISIBLE);
                AddMemory.setVisibility(View.GONE);
            }
            else{
                EditMemory.setVisibility(View.GONE);
                AddMemory.setVisibility(View.VISIBLE);
            }

            RequirementsRecyclerView.setVisibility(View.GONE);
            SaveMyRequirements.setVisibility(View.GONE);
            MyRequirementsRecyclerView.setVisibility(View.GONE);
            PlaceRecyclerView.setVisibility(View.GONE);
            AddPlace.setVisibility(View.GONE);
            Save.setVisibility(View.GONE);
            RequetsRecyclerView.setVisibility(View.GONE);
            MembersRecyclerView.setVisibility(View.GONE);
            fetchMemories();
            checkIfTripIsOld();
        }



    }


}
