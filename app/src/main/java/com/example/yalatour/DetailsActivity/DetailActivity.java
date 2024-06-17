package com.example.yalatour.DetailsActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.yalatour.Activities.CityActivity;
import com.example.yalatour.Activities.FavoriteActivity;
import com.example.yalatour.Activities.HomePage;
import com.example.yalatour.Activities.ProfileActivity;
import com.example.yalatour.Activities.TourismPlaces;
import com.example.yalatour.Activities.TripActivity;
import com.example.yalatour.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class DetailActivity extends AppCompatActivity {
    TextView detailDesc, detailTitle;
    ImageView detailImage;
    Button showTourismPlacesButton;
    ImageButton showHotelsButton,showHospitalsButton,showRestaurantsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        detailDesc = findViewById(R.id.detailDesc);
        detailImage = findViewById(R.id.detailImage);
        detailTitle = findViewById(R.id.detailTitle);
        showHotelsButton = findViewById(R.id.showHotelsButton);
        showHospitalsButton=findViewById(R.id.showHospitalsButton);
        showRestaurantsButton=findViewById(R.id.showRestaurantsButton);
        showTourismPlacesButton=findViewById(R.id.showTourismPlacesButton);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            detailDesc.setText(bundle.getString("Description"));
            detailTitle.setText(bundle.getString("Title"));
            Glide.with(this).load(bundle.getString("Image")).into(detailImage);
        }

        // Set OnClickListener for the showHotelsButton
        showHotelsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city = detailTitle.getText().toString(); // Assuming city name is in detailTitle
                String query = "hotels in " + city;

                Uri gmmIntentUri = Uri.parse("geo:?q=" + Uri.encode(query));
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");

                try {
                    startActivity(mapIntent);
                } catch (ActivityNotFoundException e) {
                    // Handle case where Google Maps is not installed
                    Toast.makeText(DetailActivity.this, "Please install Google Maps", Toast.LENGTH_SHORT).show();
                }

            }
        });
        showHospitalsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city = detailTitle.getText().toString();
                String query = "Hospitals in " + city;

                Uri gmmIntentUri = Uri.parse("geo:?q=" + Uri.encode(query));
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");

                try {
                    startActivity(mapIntent);
                } catch (ActivityNotFoundException e) {
                    // Handle case where Google Maps is not installed
                    Toast.makeText(DetailActivity.this, "Please install Google Maps", Toast.LENGTH_SHORT).show();
                }

            }
        });
        showRestaurantsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city = detailTitle.getText().toString(); // Assuming city name is in detailTitle
                String query = "Restaurants in " + city;

                Uri gmmIntentUri = Uri.parse("geo:?q=" + Uri.encode(query));
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");

                try {
                    startActivity(mapIntent);
                } catch (ActivityNotFoundException e) {
                    // Handle case where Google Maps is not installed
                    Toast.makeText(DetailActivity.this, "Please install Google Maps", Toast.LENGTH_SHORT).show();
                }

            }
        });
        showTourismPlacesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Assuming you have the city name as a string
                String cityName = detailTitle.getText().toString();

                // Create an intent to start the TourismPlacesActivity
                Intent intent = new Intent(DetailActivity.this, TourismPlaces.class);

                // Pass the city name to the TourismPlacesActivity
                intent.putExtra("cityName", cityName);

                // Start the activity
                startActivity(intent);
            }
        });
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        bottomNav.getMenu().setGroupCheckable(0, true, false);
        for (int i = 0; i < bottomNav.getMenu().size(); i++) {
            bottomNav.getMenu().getItem(i).setChecked(false);
        }

    }
    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Intent intent = null;

                    if (item.getItemId() == R.id.navigation_home) {
                        intent = new Intent(DetailActivity.this, HomePage.class);
                    } else if (item.getItemId() == R.id.navigation_trips) {
                        intent = new Intent(DetailActivity.this, TripActivity.class);
                    } else if (item.getItemId() == R.id.navigation_cities) {
                        intent = new Intent(DetailActivity.this, CityActivity.class);
                    } else if (item.getItemId() == R.id.navigation_favorites) {
                        intent = new Intent(DetailActivity.this, FavoriteActivity.class);
                    } else if (item.getItemId() == R.id.navigation_profile) {
                        intent = new Intent(DetailActivity.this, ProfileActivity.class);
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
