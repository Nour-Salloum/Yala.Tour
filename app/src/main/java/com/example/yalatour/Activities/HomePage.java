package com.example.yalatour.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.yalatour.R;

public class HomePage extends AppCompatActivity {

    Button Cities,Trips;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        Cities = findViewById(R.id.citiesButton);
        Trips=findViewById(R.id.TripsButton);

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
    }
}