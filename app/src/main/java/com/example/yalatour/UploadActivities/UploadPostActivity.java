package com.example.yalatour.UploadActivities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yalatour.Activities.HomePage;
import com.example.yalatour.Activities.ProfileActivity;
import com.example.yalatour.Adapters.ImageAdapter;
import com.example.yalatour.Classes.Post;
import com.example.yalatour.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class UploadPostActivity extends AppCompatActivity implements ImageAdapter.OnImageClickListener {
    private ProgressDialog loadingBar;
    private RecyclerView ImagesRecyclerView;
    private EditText UploadPostDescription, UploadPlaceName;
    private Button SaveButton;
    private ImageButton AddImage;
    private static final int Gallery_Pick = 1;

    private List<Uri> imageUris;
    private ImageAdapter imageAdapter;
    private String Description, PlaceName;
    private StorageReference PostsImagesReference;
    private String saveCurrentDate, saveCurrentTime, postRandomName, current_user_id;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private boolean isEditMode = false;
    private String postId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_post);

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();

        PostsImagesReference = FirebaseStorage.getInstance().getReference();
        firestore = FirebaseFirestore.getInstance();

        ImagesRecyclerView = findViewById(R.id.ImagesRecyclerView);
        ImagesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        imageUris = new ArrayList<>();
        imageAdapter = new ImageAdapter(this, imageUris, this);
        ImagesRecyclerView.setAdapter(imageAdapter);

        UploadPostDescription = findViewById(R.id.UploadPostDescription);
        UploadPlaceName = findViewById(R.id.UploadPlaceName);
        SaveButton = findViewById(R.id.SaveButton);
        AddImage = findViewById(R.id.AddImage);
        loadingBar = new ProgressDialog(this);

        // Check if in edit mode
        Intent intent = getIntent();
        if (intent.hasExtra("PostId")) {
            isEditMode = true;
            postId = intent.getStringExtra("PostId");
            String description = intent.getStringExtra("Description");
            String placeName = intent.getStringExtra("PlaceName");
            List<String> postImages = intent.getStringArrayListExtra("PostImages");

            UploadPostDescription.setText(description);
            UploadPlaceName.setText(placeName);
            for (String imageUrl : postImages) {
                imageUris.add(Uri.parse(imageUrl));
            }
            imageAdapter.notifyDataSetChanged();
        }

        AddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenGallery();
            }
        });
        SaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidatePostInfo();
            }
        });
    }

    private void ValidatePostInfo() {
        Description = UploadPostDescription.getText().toString();
        PlaceName = UploadPlaceName.getText().toString();
        if (imageUris.isEmpty()) {
            Toast.makeText(this, "Please select post image...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(Description)) {
            Toast.makeText(this, "Please say something about your image...", Toast.LENGTH_SHORT).show();
        } else {
            loadingBar.setTitle(isEditMode ? "Updating Post" : "Add New Post");
            loadingBar.setMessage("Please wait, while we are " + (isEditMode ? "updating" : "adding") + " your post...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            StoreImageToFirebaseStorage();
        }
    }

    private void StoreImageToFirebaseStorage() {
        final List<String> downloadUrls = new ArrayList<>();
        for (Uri uri : imageUris) {
            if (isEditMode && uri.toString().startsWith("http")) {
                // If in edit mode and image URI is already a URL, skip uploading
                downloadUrls.add(uri.toString());
                if (downloadUrls.size() == imageUris.size()) {
                    SavingPostInformationToDatabase(downloadUrls);
                }
            } else {
                Calendar calForDate = Calendar.getInstance();
                SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
                saveCurrentDate = currentDate.format(calForDate.getTime());

                Calendar calForTime = Calendar.getInstance();
                SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
                saveCurrentTime = currentTime.format(calForTime.getTime());

                postRandomName = saveCurrentDate + saveCurrentTime;

                StorageReference filepath = PostsImagesReference.child("Post Image").child(uri.getLastPathSegment() + postRandomName + ".jpg");

                filepath.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            filepath.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful()) {
                                        downloadUrls.add(task.getResult().toString());
                                        if (downloadUrls.size() == imageUris.size()) {
                                            SavingPostInformationToDatabase(downloadUrls);
                                        }
                                    } else {
                                        String message = task.getException().getMessage();
                                        Toast.makeText(UploadPostActivity.this, "Error occurred: " + message, Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }
                                }
                            });
                        } else {
                            String message = task.getException().getMessage();
                            Toast.makeText(UploadPostActivity.this, "Error occurred: " + message, Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                });
            }
        }
    }

    private void SavingPostInformationToDatabase(List<String> downloadUrls) {
        // Generate new date and time for editing
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(calForTime.getTime());

        firestore.collection("Users").document(current_user_id).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        String username = task.getResult().getString("username");
                        String profileImageUrl = task.getResult().getString("profileImageUrl");

                        HashMap<String, Object> postsMap = new HashMap<>();
                        postsMap.put("PostId", isEditMode ? postId : current_user_id + postRandomName);
                        postsMap.put("userId", current_user_id);
                        postsMap.put("description", Description);
                        postsMap.put("placename", PlaceName);
                        postsMap.put("postImages", downloadUrls);
                        postsMap.put("username", username);
                        postsMap.put("profileImageUrl", profileImageUrl);

                        // Always update date and time fields for edit mode
                        postsMap.put("date", saveCurrentDate);
                        postsMap.put("time", saveCurrentTime);

                        firestore.collection("Posts").document(isEditMode ? postId : current_user_id + postRandomName)
                                .set(postsMap)
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        SendUserToProfileActivity();
                                        Toast.makeText(UploadPostActivity.this, isEditMode ? "Post updated successfully." : "New post added successfully.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(UploadPostActivity.this, "Error occurred while " + (isEditMode ? "updating" : "adding") + " your post: " + task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                    loadingBar.dismiss();
                                });
                    } else {
                        Toast.makeText(UploadPostActivity.this, "Error: User not found.", Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                });
    }

    private void SendUserToProfileActivity() {
        Intent homeIntent = new Intent(UploadPostActivity.this, ProfileActivity.class);
        startActivity(homeIntent);
        finish();
    }

    private void OpenGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(galleryIntent, Gallery_Pick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    imageUris.add(imageUri);
                }
            } else if (data.getData() != null) {
                Uri imageUri = data.getData();
                imageUris.add(imageUri);
            }
            imageAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDeleteClick(int position) {
        imageUris.remove(position);
        imageAdapter.notifyItemRemoved(position);
    }
}
