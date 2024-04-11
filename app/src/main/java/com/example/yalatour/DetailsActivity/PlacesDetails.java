package com.example.yalatour.DetailsActivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.yalatour.Activities.FullScreenImageActivity;
import com.example.yalatour.R;

import java.util.ArrayList;
import java.util.List;

public class PlacesDetails extends AppCompatActivity {
    TextView PlaceName, PlaceDescription, Images, Description;
    LinearLayout PlaceImageView, Details;
    LinearLayout ImagesLayout;

    List<String> imageUrls;
    ImageButton Add, Favorite, Location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places_details);

        // Initialize views
        PlaceName = findViewById(R.id.PlaceName);
        Description = findViewById(R.id.Description);
        PlaceDescription = findViewById(R.id.PlaceDescription);
        Images = findViewById(R.id.PlaceImages);
        PlaceImageView = findViewById(R.id.PlaceImageView);
        Details = findViewById(R.id.Details);
        Add = findViewById(R.id.AddToTrip);
        Favorite = findViewById(R.id.AddToFavorite);
        Location = findViewById(R.id.Location);
        ImagesLayout = findViewById(R.id.ImagesLayout); // Add this line

        // Get extras from intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String placeName = extras.getString("placeName");
            String placeDescription = extras.getString("placeDescription");
            ArrayList<String> placeImages = extras.getStringArrayList("placeImages");

            // Set place name and description
            PlaceName.setText(placeName);
            Description.setText(placeDescription);

            // Use this list to store the image URLs
            imageUrls = new ArrayList<>();
            if (placeImages != null) {
                imageUrls.addAll(placeImages);
            }

            if (placeImages != null && !placeImages.isEmpty()) {
                String firstImageUrl = placeImages.get(0);
                Glide.with(this)
                        .load(firstImageUrl)
                        .into(new CustomTarget<Drawable>() {
                            @Override
                            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                PlaceImageView.setBackground(resource);
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {
                                // Placeholder handling if needed
                            }
                        });

            }

            Location.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String query = placeName;

                    Uri gmmIntentUri = Uri.parse("geo:?q=" + Uri.encode(query));
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");

                    try {
                        startActivity(mapIntent);
                    } catch (ActivityNotFoundException e) {
                        // Handle case where Google Maps is not installed
                        Toast.makeText(PlacesDetails.this, "Please install Google Maps", Toast.LENGTH_SHORT).show();
                    }


                }
            });

            // Handle click on Description TextView
            PlaceDescription.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Description.setVisibility(View.VISIBLE);
                    ImagesLayout.setVisibility(View.GONE);
                }
            });

            Images.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Set images layout visibility
                    ImagesLayout.setVisibility(View.VISIBLE);
                    Description.setVisibility(View.GONE); // Hide description when images are shown
                    displayImages(imageUrls);
                }
            });
        }
    }

    private void displayImages(List<String> imageUrls) {
        LinearLayout imageContainer = findViewById(R.id.ImagesLayout);
        imageContainer.removeAllViews(); // Clear previous images if any

        // Create a new vertical LinearLayout to contain rows of images
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        LinearLayout verticalLayout = new LinearLayout(this);
        verticalLayout.setLayoutParams(containerParams);
        verticalLayout.setOrientation(LinearLayout.VERTICAL);
        imageContainer.addView(verticalLayout);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(30, 25, 15, 5); // Add margins between images

        for (int i = 0; i < imageUrls.size(); i++) {
            final int count = i; // Declare 'count' as final inside the loop

            if (i % 2 == 0) {
                // If count is divisible by 2, create a new horizontal LinearLayout
                LinearLayout currentRowLayout = new LinearLayout(this);
                currentRowLayout.setLayoutParams(containerParams);
                currentRowLayout.setOrientation(LinearLayout.HORIZONTAL);
                verticalLayout.addView(currentRowLayout);
            }

            // Create ImageView for the image
            ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(layoutParams);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP); // Adjust scale type as needed

            // Load image into ImageView using Glide
            Glide.with(this)
                    .load(imageUrls.get(i))
                    .override(450, 450) // Set desired width and height
                    .centerCrop() // Scale type
                    .into(imageView);

            // Add click listener to the ImageView
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Pass the list of image URLs and the index of the clicked image to the full-screen activity
                    Intent intent = new Intent(PlacesDetails.this, FullScreenImageActivity.class);
                    intent.putStringArrayListExtra("imageUrls", (ArrayList<String>) imageUrls);
                    intent.putExtra("position", count);
                    startActivity(intent);
                }
            });

            // Find the current horizontal layout
            LinearLayout currentRowLayout = (LinearLayout) verticalLayout.getChildAt(verticalLayout.getChildCount() - 1);

            // Create a layout to hold the image and the deselect button
            LinearLayout imageLayout = new LinearLayout(this);
            imageLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            imageLayout.setOrientation(LinearLayout.VERTICAL); // Change orientation to VERTICAL
            currentRowLayout.addView(imageLayout);

            // Add the image to the current horizontal layout
            imageLayout.addView(imageView);
        }
    }

}
