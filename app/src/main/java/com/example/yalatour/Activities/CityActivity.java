package com.example.yalatour.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.yalatour.Adapters.MyAdapter;
import com.example.yalatour.Classes.CityClass;
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
    private RecyclerView recyclerView;
    private List<CityClass> cityList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city);

        fab = findViewById(R.id.fab);
        recyclerView = findViewById(R.id.recyclerView);
        // Fetch current user's data and check admin status
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();

            db.collection("Users").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String isAdmin = documentSnapshot.getString("isAdmin");

                            // Show or hide FAB based on admin status
                            if (isAdmin != null && isAdmin.equals("1")) {
                                fab.setVisibility(View.VISIBLE);
                            } else {
                                fab.setVisibility(View.GONE);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Handle failure to fetch user data
                    });
        } else {
            // User is not signed in, handle accordingly
        }




        // Setting up RecyclerView with GridLayoutManager
        GridLayoutManager gridLayoutManager = new GridLayoutManager(CityActivity.this, 1);
        recyclerView.setLayoutManager(gridLayoutManager);

        // Showing progress dialog while fetching data
        AlertDialog.Builder builder = new AlertDialog.Builder(CityActivity.this);
        builder.setCancelable(false);
        builder.setView(R.layout.activity_progress);
        AlertDialog dialog = builder.create();
        dialog.show();

        cityList = new ArrayList<>();

        // Creating adapter and setting it to RecyclerView
        MyAdapter adapter = new MyAdapter(CityActivity.this, cityList);
        recyclerView.setAdapter(adapter);

        // Fetching data from Firestore
        db.collection("Cities")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        cityList.clear(); // Clear existing data (optional)
                        for (DocumentSnapshot document : task.getResult()) {
                            CityClass cityClass = document.toObject(CityClass.class);
                            cityList.add(cityClass);
                        }
                        adapter.notifyDataSetChanged();
                        dialog.dismiss();
                    } else {
                        dialog.dismiss();
                        // Handle errors
                    }
                });


        // Handling FAB click
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CityActivity.this, UploadActivity.class);
                startActivity(intent);
            }
        });
    }
}
