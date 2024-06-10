package com.example.yalatour.UploadActivities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.yalatour.Activities.HomePage;
import com.example.yalatour.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class UploadPostActivity extends AppCompatActivity {
    private ProgressDialog loadingBar;
    private ImageView UploadPostImage;
    private EditText UploadPostDescription, UploadPlaceName;

    private Button SaveButton;
    private static final int Gallery_Pick = 1;

    private Uri ImageUri;
    private String Description, PlaceName;
    private StorageReference PostsImagesReference;
    private String saveCurrentDate, saveCurrentTime, postRandomName, downloadUrl, current_user_id;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_post);

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();

        PostsImagesReference = FirebaseStorage.getInstance().getReference();
        firestore = FirebaseFirestore.getInstance();

        UploadPostImage = findViewById(R.id.UploadPostImage);
        UploadPostDescription = findViewById(R.id.UploadPostDescription);
        UploadPlaceName = findViewById(R.id.UploadPlaceName);
        SaveButton = findViewById(R.id.SaveButton);
        loadingBar = new ProgressDialog(this);

        UploadPostImage.setOnClickListener(new View.OnClickListener() {
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
        if (ImageUri == null) {
            Toast.makeText(this, "Please select post image...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(Description)) {
            Toast.makeText(this, "Please say something about your image...", Toast.LENGTH_SHORT).show();
        } else {
            loadingBar.setTitle("Add New Post");
            loadingBar.setMessage("Please wait, while we are updating your new post...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            StoreImageToFirebaseStorage();
        }
    }

    private void StoreImageToFirebaseStorage() {
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(calForTime.getTime());

        postRandomName = saveCurrentDate + saveCurrentTime;

        StorageReference filepath = PostsImagesReference.child("Post Image").child(ImageUri.getLastPathSegment() + postRandomName + ".jpg");

        filepath.putFile(ImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    filepath.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                downloadUrl = task.getResult().toString();
                                Toast.makeText(UploadPostActivity.this, "Image uploaded successfully to storage...", Toast.LENGTH_SHORT).show();
                                SavingPostInformationToDatabase();
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

    private void SavingPostInformationToDatabase() {
        firestore.collection("Users").document(current_user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult().exists()) {
                    String username = task.getResult().getString("username");

                    HashMap<String, Object> postsMap = new HashMap<>();
                    postsMap.put("PostId", current_user_id);
                    postsMap.put("date", saveCurrentDate);
                    postsMap.put("time", saveCurrentTime);
                    postsMap.put("description", Description);
                    postsMap.put("placename", PlaceName);
                    postsMap.put("postimage", downloadUrl);
                    postsMap.put("username", username);

                    firestore.collection("Posts").document(current_user_id + postRandomName)
                            .set(postsMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        SendUserToHomePage();
                                        Toast.makeText(UploadPostActivity.this, "New post is updated successfully.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(UploadPostActivity.this, "Error occurred while updating your post: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                    loadingBar.dismiss();
                                }
                            });
                } else {
                    Toast.makeText(UploadPostActivity.this, "Error: User not found.", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
            }
        });
    }

    private void SendUserToHomePage() {
        Intent homeIntent = new Intent(UploadPostActivity.this, HomePage.class);
        startActivity(homeIntent);
        finish();
    }

    private void OpenGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, Gallery_Pick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null) {
            ImageUri = data.getData();
            UploadPostImage.setImageURI(ImageUri);
        }
    }
}
