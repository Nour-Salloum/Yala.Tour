package com.example.yalatour.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.yalatour.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private CircleImageView profileImage;
    private ImageButton changeImageButton;
    private Button saveChangesButton;

    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private FirebaseUser currentUser;
    private StorageReference storageReference;

    private Uri profileImageUri;
    private String currentUserId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize Firebase components
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        currentUser = fAuth.getCurrentUser();

        // Initialize views
        profileImage = findViewById(R.id.ProfileImage);
        changeImageButton = findViewById(R.id.ChangeImage);
        saveChangesButton = findViewById(R.id.SaveChanges);

        // Load current user data
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            loadUserData();
        }

        // Set click listener for change image button
        changeImageButton.setOnClickListener(v -> openGallery());

        // Set click listener for save changes button
        saveChangesButton.setOnClickListener(v -> saveChanges());
    }

    private void loadUserData() {
        DocumentReference userRef = fStore.collection("Users").document(currentUserId);
        userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    // Retrieve user data and load profile image
                    String profileImageUrl = documentSnapshot.getString("profileImageUrl");
                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                        Glide.with(EditProfileActivity.this).load(profileImageUrl).into(profileImage);
                    } else {
                        // Set default image if no image found
                        profileImage.setImageResource(R.drawable.baseline_person_24);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EditProfileActivity.this, "Failed to load user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(galleryIntent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private void saveChanges() {
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        if (profileImageUri != null) {
            // Upload new profile image to Firebase Storage
            StorageReference profileImageRef = storageReference.child("profile_images").child(currentUserId + ".jpg");
            profileImageRef.putFile(profileImageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Get the download URL of the uploaded image
                        profileImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            // Update profile image URL in Firestore
                            updateProfileImageUrl(uri.toString());
                        });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(EditProfileActivity.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // No new image selected, just finish activity
            finish();
        }
    }

    private void updateProfileImageUrl(String imageUrl) {
        DocumentReference userRef = fStore.collection("Users").document(currentUserId);
        userRef.update("profileImageUrl", imageUrl)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EditProfileActivity.this, "Profile image updated successfully", Toast.LENGTH_SHORT).show();
                    updateProfileImageInPosts(imageUrl); // Update profile image in posts
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EditProfileActivity.this, "Failed to update profile image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateProfileImageInPosts(String imageUrl) {
        fStore.collection("Posts")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    WriteBatch batch = fStore.batch();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        DocumentReference postRef = document.getReference();
                        batch.update(postRef, "profileImageUrl", imageUrl);
                    }
                    batch.commit()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(EditProfileActivity.this, "Profile image updated in posts successfully", Toast.LENGTH_SHORT).show();
                                updateProfileImageInComments(imageUrl); // Update profile image in comments
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(EditProfileActivity.this, "Failed to update profile image in posts: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EditProfileActivity.this, "Failed to retrieve posts: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateProfileImageInComments(String imageUrl) {
        fStore.collection("Comments")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    WriteBatch batch = fStore.batch();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        DocumentReference commentRef = document.getReference();
                        batch.update(commentRef, "profileImageUrl", imageUrl);
                    }
                    batch.commit()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(EditProfileActivity.this, "Profile image updated in comments successfully", Toast.LENGTH_SHORT).show();
                                // Create an intent to hold the result data
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("profileImageUrl", imageUrl);
                                // Set the result and finish the activity
                                setResult(RESULT_OK, resultIntent);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(EditProfileActivity.this, "Failed to update profile image in comments: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EditProfileActivity.this, "Failed to retrieve comments: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            profileImageUri = data.getData();
            profileImage.setImageURI(profileImageUri);
        }
    }
}



