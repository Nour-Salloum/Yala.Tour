package com.example.yalatour.DetailsActivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.yalatour.Activities.CityActivity;
import com.example.yalatour.Activities.FullScreenImageActivity;
import com.example.yalatour.Adapters.MyAdapter;
import com.example.yalatour.Adapters.ReviewAdapter;
import com.example.yalatour.Classes.CityClass;
import com.example.yalatour.Classes.PlaceReviewClass;
import com.example.yalatour.Classes.RatingClass;
import com.example.yalatour.R;
import com.example.yalatour.UploadActivities.UploadPlaceActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PlacesDetails extends AppCompatActivity {
    TextView PlaceName, Description,AllReviews,Total,RateThePlace;

    RelativeLayout PlaceImageView;
    List<String> imageUrls;
    ImageButton Add, Favorite, Location;
    ImageView backgroundImageView;
    int currentPosition = 0;
    LinearLayout indicatorLayout;
    List<ImageView> indicators;
    boolean isActive = false;
    RecyclerView Review_RecyclerView;
    Button Send;
    EditText ReviewText;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private List<PlaceReviewClass> Reviews;
    private ReviewAdapter adapter;
    private RatingBar PlaceRating;
    float TotalRating;
    String PlaceId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places_details);

        // Initialize views
        PlaceName = findViewById(R.id.PlaceName);
        Description = findViewById(R.id.Description);
        PlaceImageView = findViewById(R.id.PlaceImageView);
        Add = findViewById(R.id.AddToTrip);
        Favorite = findViewById(R.id.AddToFavorite);
        Location = findViewById(R.id.Location);
        backgroundImageView = findViewById(R.id.backgroundImageView);
        PlaceRating=findViewById(R.id.placeRatingBar);
        AllReviews=findViewById(R.id.AllReviews);
        RateThePlace=findViewById(R.id.RateThePlace);
        isActive = true;
        Review_RecyclerView=findViewById(R.id.Review_RecyclerView);
        Send=findViewById(R.id.Send);
        ReviewText=findViewById(R.id.ReviewText);
        Total=findViewById(R.id.PlaceRate);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Get extras from intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            PlaceId=extras.getString("PlaceId");
            String placeName = extras.getString("placeName");
            String placeDescription = extras.getString("placeDescription");
            ArrayList<String> placeImages = extras.getStringArrayList("placeImages");
            TotalRating = getIntent().getFloatExtra("TotalRating", 0.0f);

            // Set place name and description
            PlaceName.setText(placeName);
            Description.setText(placeDescription);

            // Use this list to store the image URLs
            imageUrls = new ArrayList<>();
            if (placeImages != null) {
                imageUrls.addAll(placeImages);
            }

            // Load the first image initially
            if (!imageUrls.isEmpty()) {
                Glide.with(this)
                        .load(imageUrls.get(0))
                        .into(backgroundImageView);


            }
            indicatorLayout = findViewById(R.id.indicatorLayout);
            indicators = new ArrayList<>();

            // Add indicators dynamically
            for (int i = 0; i < imageUrls.size(); i++) {
                ImageView indicator = createIndicator();
                final int position = i; // Final to access inside the click listener
                indicator.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Update current position and load corresponding image
                        currentPosition = position;
                        Glide.with(PlacesDetails.this)
                                .load(imageUrls.get(currentPosition))
                                .into(backgroundImageView);
                        // Update indicators
                        updateIndicators(currentPosition);
                    }
                });
                indicators.add(indicator);
                indicatorLayout.addView(indicator);
            }

            // Start auto image slider
            startImageSlider();
        }
        Send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveReviewData();
            }
        });
        GridLayoutManager gridLayoutManager = new GridLayoutManager(PlacesDetails.this, 1);
        Review_RecyclerView.setLayoutManager(gridLayoutManager);
        Reviews = new ArrayList<>();

        // Creating adapter and setting it to RecyclerView
        adapter = new ReviewAdapter(PlacesDetails.this, Reviews);
        Review_RecyclerView.setAdapter(adapter);

        // Fetching data from Firestore
        fetchReviews();
        AllReViewsVisibilty();
        AllReviews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchAllReviews();
                AllReviews.setVisibility(View.GONE);
            }
        });

        // Inside your onCreate() method

        Total.setText(TotalRating + "/5");
        getRatingForCurrentUser();
// When the user rates a place
        PlaceRating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {

                // Save/update the rating in the Firestore database
                saveOrUpdateRatingInDatabase(rating); // Implement this method to save/update the rating in the database
            }
        });
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();

            db.collection("Users").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String isUser = documentSnapshot.getString("isUser");


                            // Show or hide FAB based on admin status
                            if (isUser != null && isUser.equals("1")) {
                                ReviewText.setVisibility(View.VISIBLE);
                                Send.setVisibility(View.VISIBLE);
                                PlaceRating.setVisibility(View.VISIBLE);
                                RateThePlace.setVisibility(View.VISIBLE);
                            } else {
                                ReviewText.setVisibility(View.GONE);
                                Send.setVisibility(View.GONE);
                                PlaceRating.setVisibility(View.GONE);
                                RateThePlace.setVisibility(View.GONE);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("TourismPlaces", "Failed to fetch user data", e);
                    });
        } else {
            // User is not signed in, handle accordingly
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        isActive = false;
    }

    private void startImageSlider() {
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (!isActive) {
                    // Stop the image slider if activity is no longer active
                    return;
                }
                if (currentPosition == imageUrls.size()) {
                    currentPosition = 0;
                }
                Glide.with(PlacesDetails.this)
                        .load(imageUrls.get(currentPosition))
                        .into(backgroundImageView);
                updateIndicators(currentPosition);
                currentPosition++;
                handler.postDelayed(this, 3000); // Change image every 3 seconds
            }
        };
        handler.postDelayed(runnable, 3000); // Start auto sliding after 3 seconds
    }
    // Method to create an individual indicator
    private ImageView createIndicator() {
        ImageView indicator = new ImageView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(8, 0, 8, 0); // Adjust margins as needed
        indicator.setLayoutParams(params);
        indicator.setImageResource(R.drawable.indicator_inactive); // Set inactive indicator image
        return indicator;
    }

    // Method to update indicators based on current position
    private void updateIndicators(int position) {
        for (int i = 0; i < indicators.size(); i++) {
            if (i == position) {
                indicators.get(i).setImageResource(R.drawable.indicator_active); // Set active indicator image
            } else {
                indicators.get(i).setImageResource(R.drawable.indicator_inactive); // Set inactive indicator image
            }
        }
    }
    private  void SaveReviewData() {

        String reviewText = ReviewText.getText().toString().trim();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = currentUser.getUid();
        db.collection("Users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("Username");
                        if (!reviewText.isEmpty()){
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                            String dateString = dateFormat.format(new Date());
                            PlaceReviewClass Review=new PlaceReviewClass(null, username, PlaceId, dateString, reviewText);
                            db.collection("Reviews").add(Review)
                                    .addOnSuccessListener(documentReference -> {
                                        String ReviewId = documentReference.getId();
                                        Review.setReviewId(ReviewId);
                                        documentReference.set(Review)
                                                .addOnSuccessListener(aVoid -> {
                                                    // Handle success
                                                    Toast.makeText(PlacesDetails.this, "Your Review is Added Thank You", Toast.LENGTH_SHORT).show();
                                                    // After successfully adding the review, fetch the updated review list and refresh the RecyclerView
                                                    fetchReviews();
                                                    ReviewText.setText("");
                                                })
                                                .addOnFailureListener(e -> {
                                                    // Handle failure
                                                    Toast.makeText(PlacesDetails.this, "Failed to Add the Review: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                });
                                    });
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure to fetch user data
                });
    }

    private void fetchReviews() {
        db.collection("Reviews")
                .whereEqualTo("review_placeid", PlaceId)
                .limit(3) // Limit to fetch only the latest 3 reviews
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Reviews.clear(); // Clear existing data (optional)
                        for (DocumentSnapshot document : task.getResult()) {
                            PlaceReviewClass ReviewClass = document.toObject(PlaceReviewClass.class);
                            Reviews.add(ReviewClass);
                        }
                        adapter.notifyDataSetChanged(); // Notify adapter of data change


                    } else {
                        // Handle errors
                    }
                });
    }
    private void fetchAllReviews() {
        db.collection("Reviews")
                .whereEqualTo("review_placeid", PlaceId)

                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Reviews.clear(); // Clear existing data (optional)
                        for (DocumentSnapshot document : task.getResult()) {
                            PlaceReviewClass ReviewClass = document.toObject(PlaceReviewClass.class);
                            Reviews.add(ReviewClass);
                        }
                        adapter.notifyDataSetChanged(); // Notify adapter of data change

                    } else {
                        // Handle errors
                    }
                });
    }
    private void AllReViewsVisibilty() {
        db.collection("Reviews")
                .whereEqualTo("review_placeid", PlaceId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Fetching reviews successful, now check the size
                        int reviewsSize = task.getResult().size();
                        if (reviewsSize <= 3) {
                            AllReviews.setVisibility(View.GONE);
                        }
                    } else {
                        // Handle errors
                        Log.e("PlacesDetails", "Error fetching reviews: ", task.getException());
                    }
                });
    }


    private void saveOrUpdateRatingInDatabase(float rating) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = currentUser.getUid();

        db.collection("Ratings")
                .whereEqualTo("rating_Placeid", PlaceId)
                .whereEqualTo("rating_userid", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            // Existing rating found, update the rating value
                            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                            String ratingId = documentSnapshot.getId();
                            db.collection("Ratings").document(ratingId)
                                    .update("ratingvalue", rating)
                                    .addOnSuccessListener(aVoid -> {
                                        // Rating updated successfully
                                        // Update UI
                                        PlaceRating.setRating(rating);
                                        CalculateRatingTotal();
                                    })
                                    .addOnFailureListener(e -> {
                                        // Handle update failure
                                    });
                        } else {
                            // No existing rating found, create a new document
                            RatingClass Rating = new RatingClass(null, PlaceId, userId, rating);
                            db.collection("Ratings").add(Rating)
                                    .addOnSuccessListener(documentReference -> {
                                        String RatingId = documentReference.getId();
                                        Rating.setRatingid(RatingId);
                                        documentReference.set(Rating);
                                        PlaceRating.setRating(rating);
                                        CalculateRatingTotal();
                                    })
                                    .addOnFailureListener(e -> {
                                        // Handle save failure
                                    });
                        }
                    } else {
                        // Handle task failure
                    }
                });
    }



    private void CalculateRatingTotal(){
        db.collection("Ratings")
                .whereEqualTo("rating_Placeid",PlaceId)
                .get()
                .addOnCompleteListener(task -> {
                    int nbRatings=0;
                    float AllRatings=0;
                    for (DocumentSnapshot document : task.getResult()) {
                        RatingClass rating = document.toObject(RatingClass.class);
                        AllRatings += rating.getRatingvalue();
                        nbRatings++;
                    }
                    if (nbRatings > 0) {
                        TotalRating = AllRatings / nbRatings;

                        db.collection("TourismPlaces").document(PlaceId)
                                .update("totalRating", TotalRating).addOnSuccessListener(aVoid -> {
                                    Total.setText(TotalRating + "/5");

                        }).addOnFailureListener(e -> {
                            // Handle transaction failure
                            Log.w("PlacesDetails", "Failed to update total rating: ", e);
                        });

                    }

    });
    }
    private void getRatingForCurrentUser() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("Ratings")
                    .whereEqualTo("rating_Placeid", PlaceId)
                    .whereEqualTo("rating_userid", userId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (!task.getResult().isEmpty()) {
                                // Existing rating found, set the rating value in the rating bar
                                DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                                float userRating = documentSnapshot.getDouble("ratingvalue").floatValue();
                                PlaceRating.setRating(userRating);
                            }
                        } else {
                            // Handle task failure
                        }
                    });
        }
    }


}
