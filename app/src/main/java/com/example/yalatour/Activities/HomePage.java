package com.example.yalatour.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import android.view.View;

import android.widget.ImageButton;


import com.example.yalatour.Adapters.PostsAdapter;

import com.example.yalatour.R;

import com.example.yalatour.UploadActivities.UploadPostActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class HomePage extends AppCompatActivity {

    ImageButton Cities,Trips;

    private RecyclerView postList;
    private PostsAdapter adapter;
    private FloatingActionButton AddPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        Cities = findViewById(R.id.CitiesButton);
        Trips=findViewById(R.id.TripsButton);
        AddPost=findViewById(R.id.AddPost);


        Cities.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent =new Intent(HomePage.this, CityActivity.class);
                startActivity(intent);
            }

        });
        Trips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent(HomePage.this, TripActivity.class);
                startActivity(intent);
            }
        });
        setupFab();

        postList = (RecyclerView) findViewById(R.id.PostsRecyclerView);
        postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);

        adapter = new PostsAdapter(this);
        postList.setAdapter(adapter);


    }

    private void setupFab() {
        AddPost.setOnClickListener(view -> {
            Intent intent = new Intent(HomePage.this, UploadPostActivity.class);
            startActivity(intent);
        });
    }
}