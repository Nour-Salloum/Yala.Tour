package com.example.yalatour.Activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.yalatour.Adapters.CommentsAdapter;
import com.example.yalatour.Adapters.ImagePagerAdapter;
import com.example.yalatour.Classes.Comment;
import com.example.yalatour.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsActivity extends AppCompatActivity {

    private CircleImageView profileImageView;
    private ImageView PostImage;
    private TextView usernameTextView, dateTextView, descriptionTextView, placeNameTextView, timeTextView;
    private RecyclerView commentsRecyclerView;
    private CommentsAdapter commentsAdapter;
    private String postId;
    private EditText CommentText;
    private ViewPager2 postImagePager; // ViewPager2 for displaying images

    private static final String TAG = "CommentsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        // Initialize views
        profileImageView = findViewById(R.id.ProfileImage);
        usernameTextView = findViewById(R.id.PostUsername);
        dateTextView = findViewById(R.id.post_date);
        timeTextView = findViewById(R.id.post_time);
        descriptionTextView = findViewById(R.id.PostDescription);
        placeNameTextView = findViewById(R.id.PlaceName);
        commentsRecyclerView = findViewById(R.id.CommentsRecyclerView);
        CommentText = findViewById(R.id.CommentText);
        postImagePager = findViewById(R.id.PostImagePager); // Initialize ViewPager2

        // Get post data from intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            postId = extras.getString("postId");
            List<String> postImages = extras.getStringArrayList("postImages");

            String username = extras.getString("username");
            String date = extras.getString("date");
            String description = extras.getString("description");
            String placeName = extras.getString("placeName");
            String profileImageUrl = extras.getString("profileImageUrl");
            String time = extras.getString("time");

            // Set post details
            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                Picasso.get().load(profileImageUrl).into(profileImageView);
            } else {
                profileImageView.setImageResource(R.drawable.baseline_person_24);
            }

            usernameTextView.setText(username);
            dateTextView.setText(date);
            timeTextView.setText(time);
            descriptionTextView.setText(description);
            placeNameTextView.setText(placeName);

            // Load the first image into the ViewPager2 using ImagePagerAdapter
            if (postImages != null && !postImages.isEmpty()) {
                ImagePagerAdapter imagePagerAdapter = new ImagePagerAdapter(this, postImages);
                postImagePager.setAdapter(imagePagerAdapter);
                // Setup image indicators
                setupImageIndicators(postImages.size());
            }

            // Load comments related to this post
            loadComments(postId);
        }

        // Set click listener for Send button
        findViewById(R.id.Send).setOnClickListener(v -> sendComment());
    }

    // Method to setup image indicators
    private void setupImageIndicators(int numImages) {
        LinearLayout imageIndicatorContainer = findViewById(R.id.imageIndicatorContainer);
        imageIndicatorContainer.removeAllViews(); // Clear existing indicators

        // Create indicators for each image
        ImageView[] indicators = new ImageView[numImages];
        for (int i = 0; i < numImages; i++) {
            indicators[i] = new ImageView(this);
            indicators[i].setImageResource(R.drawable.indicator_inactive); // Set inactive indicator initially

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(8, 0, 8, 0); // Adjust margins as needed
            indicators[i].setLayoutParams(layoutParams);

            imageIndicatorContainer.addView(indicators[i]);
        }

        // Highlight the first indicator initially
        if (numImages > 0) {
            indicators[0].setImageResource(R.drawable.indicator_active); // Set the first indicator active
        }

        // ViewPager2 page change listener to update indicators
        postImagePager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                // Update indicators based on current page
                for (int i = 0; i < numImages; i++) {
                    // Calculate the "opposite" position
                    int oppositePosition = (numImages - 1) - position;

                    indicators[i].setImageResource(
                            i == oppositePosition ? R.drawable.indicator_active : R.drawable.indicator_inactive);
                }
            }
        });
    }

    private void loadComments(String postId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Comments")
                .whereEqualTo("postId", postId)
                .orderBy("commentDate")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Comment> comments = queryDocumentSnapshots.toObjects(Comment.class);

                    // Bind comments to RecyclerView using CommentsAdapter
                    commentsAdapter = new CommentsAdapter(this, comments);
                    commentsRecyclerView.setAdapter(commentsAdapter);
                    commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                })
                .addOnFailureListener(e -> {
                    // Handle failure to load comments
                    Log.e(TAG, "Failed to load comments", e);
                });
    }

    private void sendComment() {
        String commentText = CommentText.getText().toString().trim();
        if (!TextUtils.isEmpty(commentText)) {
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("Users").document(currentUserId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String username = documentSnapshot.getString("username");
                            String profileImageUrl = documentSnapshot.getString("profileImageUrl");
                            String commentDate = getCurrentDateTime();

                            Comment comment = new Comment(null, username, postId, commentDate, commentText, profileImageUrl);

                            FirebaseFirestore dbComments = FirebaseFirestore.getInstance();
                            dbComments.collection("Comments")
                                    .add(comment)
                                    .addOnSuccessListener(documentReference -> {
                                        Toast.makeText(CommentsActivity.this, "Comment added successfully", Toast.LENGTH_SHORT).show();
                                        CommentText.setText("");
                                        loadComments(postId); // Reload comments to reflect the new comment
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(CommentsActivity.this, "Failed to add comment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        Log.e(TAG, "Error adding comment", e);
                                    });
                        } else {
                            Toast.makeText(CommentsActivity.this, "User document doesn't exist", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(CommentsActivity.this, "Failed to fetch user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error fetching user data", e);
                    });
        } else {
            Toast.makeText(this, "Comment cannot be empty", Toast.LENGTH_SHORT).show();
        }
    }

    private String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }
}
