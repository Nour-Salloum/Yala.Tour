package com.example.yalatour.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.yalatour.Adapters.ProfilePostsAdapter;
import com.example.yalatour.R;
import com.example.yalatour.UploadActivities.UploadPostActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private RecyclerView profilepostList;
    private ProfilePostsAdapter adapter;
    private FloatingActionButton AddPost;
    private ImageButton Logout;
    private CircleImageView profileImage;
    private TextView profileUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Set the status bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.dark_blue));
        }

        profileImage = findViewById(R.id.ProfileProfileImage);
        profileUsername = findViewById(R.id.ProfileUsername);
        Logout = findViewById(R.id.Logout);
        AddPost = findViewById(R.id.AddPost);

        //Logout Method
        Logout.setOnClickListener(view -> showLogoutConfirmationDialog());

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        bottomNav.getMenu().setGroupCheckable(0, true, false);
        for (int i = 0; i < bottomNav.getMenu().size(); i++) {
            bottomNav.getMenu().getItem(i).setChecked(false);
        }

        profilepostList = findViewById(R.id.ProfilePostsRecyclerView);
        profilepostList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        profilepostList.setLayoutManager(linearLayoutManager);

        adapter = new ProfilePostsAdapter(this);
        profilepostList.setAdapter(adapter);

        setupFab();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.ProfileActivity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loadUserProfile();
    }

    private void showLogoutConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logout");
        builder.setMessage("Are you sure you want to logout?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                logoutUser();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // User cancelled the dialog
            }
        });
        builder.create().show();
    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(ProfileActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(ProfileActivity.this, LoginPage.class));
        finish();
    }

    private void setupFab() {
        AddPost.setOnClickListener(view -> {
            Intent intent = new Intent(ProfileActivity.this, UploadPostActivity.class);
            startActivity(intent);
        });
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            item -> {
                Intent intent = null;

                if (item.getItemId() == R.id.navigation_home) {
                    intent = new Intent(ProfileActivity.this, HomePage.class);
                } else if (item.getItemId() == R.id.navigation_trips) {
                    intent = new Intent(ProfileActivity.this, TripActivity.class);
                } else if (item.getItemId() == R.id.navigation_cities) {
                    intent = new Intent(ProfileActivity.this, CityActivity.class);
                } else if (item.getItemId() == R.id.navigation_favorites) {
                    intent = new Intent(ProfileActivity.this, FavoritePage.class);
                } else if (item.getItemId() == R.id.navigation_profile) {
                    intent = new Intent(ProfileActivity.this, ProfileActivity.class);
                }

                if (intent != null) {
                    intent.putExtra("menuItemId", item.getItemId());
                    startActivity(intent);
                    overridePendingTransition(0, 0); // No animation
                    return true;
                }

                return false;
            };

    private void loadUserProfile() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("Users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");
                        String profileImageUrl = documentSnapshot.getString("profileImageUrl");

                        profileUsername.setText(username);

                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            Glide.with(this).load(profileImageUrl).into(profileImage);
                        } else {
                            profileImage.setImageResource(R.drawable.baseline_person2_24);
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(ProfileActivity.this, "Failed to load user profile", Toast.LENGTH_SHORT).show());
    }
}
