package com.example.yalatour.UploadActivities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import com.bumptech.glide.Glide;
import com.example.yalatour.Classes.MemoriesClass;
import com.example.yalatour.R;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class UploadMemoryActivity extends AppCompatActivity {

    EditText MemoryText;
    Button saveButton;
    ImageButton AddText;
    List<String> imageUrls;
    List<String> Videos;
    List<String> Texts;
    String currentUserId;
    String tripId;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    FirebaseFirestore db;
    LinearLayout MemoryContainer, TextLayout, dynamicTextContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_upload_memory);

        MemoryText = findViewById(R.id.MemoryText);
        saveButton = findViewById(R.id.saveMemoryButton);
        MemoryContainer = findViewById(R.id.MemoryContainer);
        dynamicTextContainer = findViewById(R.id.dynamicTextContainer);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        currentUserId = currentUser.getUid();
        imageUrls = new ArrayList<>();
        Videos = new ArrayList<>();
        tripId = getIntent().getStringExtra("tripId");
        AddText = findViewById(R.id.AddText);
        TextLayout = findViewById(R.id.TextLayout);
        Texts = getNonEmptyTexts();

        MemoryContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPicker();
            }
        });

        AddText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MemoryText.getText().toString().trim().isEmpty()) {
                    Toast.makeText(UploadMemoryActivity.this, "Please enter text before adding", Toast.LENGTH_SHORT).show();
                } else {
                    addNewMemoryText();
                }
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get non-empty texts
                Texts = new ArrayList<>();

                // Get text from MemoryText field
                String memoryText = MemoryText.getText().toString().trim();

                // Check if MemoryText is not empty
                if (!memoryText.isEmpty()) {
                    // Add MemoryText to the Texts list
                    Texts.add(memoryText);
                }
                Texts.addAll(getNonEmptyTexts());

                // Check if any media has been added
                if (imageUrls.isEmpty() && Videos.isEmpty() && Texts.isEmpty()) {
                    // None of the fields are filled, show a toast message
                    Toast.makeText(UploadMemoryActivity.this, "Please enter at least a text, image, or video", Toast.LENGTH_SHORT).show();
                    return; // Exit the onClick method, do not proceed further
                }

                // Create MemoriesClass object
                MemoriesClass memory = new MemoriesClass(null, currentUserId, tripId, imageUrls, Videos, Texts);

                // Add memory to Firestore
                db.collection("Memories").add(memory).addOnSuccessListener(documentReference -> {
                    String MemoryId = documentReference.getId();
                    memory.setMemoryId(MemoryId);
                    documentReference.set(memory)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(UploadMemoryActivity.this, "Memory saved successfully", Toast.LENGTH_SHORT).show();
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("MemoryAdded", true);
                                setResult(RESULT_OK, resultIntent);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(UploadMemoryActivity.this, "Failed to save memory: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                });
            }
        });
    }

    private void openPicker() {
        Intent mediaPicker = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        mediaPicker.setType("*/*");
        String[] mimeTypes = {"image/*", "video/*"};
        mediaPicker.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        mediaPicker.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(mediaPicker, 1);
    }

    private void addNewMemoryText() {
        String memoryTextContent = MemoryText.getText().toString().trim();
        if (!memoryTextContent.isEmpty()) {
            EditText newMemoryText = new EditText(this);

            // Set fixed width and height
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    815, // Width in pixels
                    170  // Height in pixels
            );
            layoutParams.setMargins(0, 15, 0, 0);
            newMemoryText.setLayoutParams(layoutParams);

            // Set other attributes
            newMemoryText.setId(ViewCompat.generateViewId());
            newMemoryText.setHint("Enter Text");
            newMemoryText.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            newMemoryText.setBackground(ContextCompat.getDrawable(this, R.drawable.lavender_boarder));
            newMemoryText.setPadding(20, 16, 16, 16);
            newMemoryText.setTextColor(ContextCompat.getColor(this, R.color.black));
            newMemoryText.setText(memoryTextContent);

            dynamicTextContainer.addView(newMemoryText);

            // Clear the MemoryText field
            MemoryText.setText("");
        }
    }

    private List<String> getNonEmptyTexts() {
        List<String> nonEmptyTexts = new ArrayList<>();

        // Iterate through all child views in dynamicTextContainer
        for (int i = 0; i < dynamicTextContainer.getChildCount(); i++) {
            View view = dynamicTextContainer.getChildAt(i);

            // Check if the view is an instance of EditText
            if (view instanceof EditText) {

                EditText editText = (EditText) view;
                String text = editText.getText().toString().trim();

                if (!text.isEmpty()) {
                    nonEmptyTexts.add(text);
                }
            }
        }

        return nonEmptyTexts;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            if (data != null) {
                if (data.getClipData() != null) {
                    // If multiple media files are selected
                    int count = data.getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {
                        Uri mediaUri = data.getClipData().getItemAt(i).getUri();
                        String type = getContentResolver().getType(mediaUri);
                        if (type.startsWith("image/")) {
                            uploadMediaToStorage(mediaUri, "image");
                        } else if (type.startsWith("video/")) {
                            uploadMediaToStorage(mediaUri, "video");
                        }
                    }
                } else if (data.getData() != null) {
                    // If a single media file is selected
                    Uri mediaUri = data.getData();
                    String type = getContentResolver().getType(mediaUri);
                    if (type.startsWith("image/")) {
                        uploadMediaToStorage(mediaUri, "image");
                    } else if (type.startsWith("video/")) {
                        uploadMediaToStorage(mediaUri, "video");
                    }
                }
                displaySelectedMedia(imageUrls, Videos);
                MemoryContainer.setBackground(null);
            }
        }
    }

    private void displaySelectedMedia(List<String> imageUrls, List<String> videoUrls) {
        LinearLayout mediaContainer = findViewById(R.id.MemoryContainer);
        mediaContainer.removeAllViews();

        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        LinearLayout verticalLayout = new LinearLayout(this);
        verticalLayout.setLayoutParams(containerParams);
        verticalLayout.setOrientation(LinearLayout.VERTICAL);
        mediaContainer.addView(verticalLayout);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(10, 10, 20, 0);

        int count = 0;
        LinearLayout currentRowLayout = null;

        // Display images
        for (String imageUrl : imageUrls) {
            if (count % 3 == 0) {
                currentRowLayout = new LinearLayout(this);
                currentRowLayout.setLayoutParams(containerParams);
                currentRowLayout.setOrientation(LinearLayout.HORIZONTAL);
                verticalLayout.addView(currentRowLayout);
            }

            FrameLayout imageFrameLayout = new FrameLayout(this);
            imageFrameLayout.setLayoutParams(layoutParams);

            ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
            ));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            // Load image using Glide library
            Glide.with(this)
                    .load(imageUrl)
                    .override(250, 250)
                    .centerCrop()
                    .into(imageView);

            Button deselectButton = new Button(this);
            FrameLayout.LayoutParams buttonLayoutParams = new FrameLayout.LayoutParams(
                    80,
                    80
            );
            buttonLayoutParams.setMargins(0, 0, 0, 0);
            deselectButton.setPadding(0, 5, 0, 0);
            deselectButton.setLayoutParams(buttonLayoutParams);
            deselectButton.setText("X");

            deselectButton.setTextColor(getResources().getColor(R.color.black));
            deselectButton.setBackgroundColor(getResources().getColor(R.color.red));
            deselectButton.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            // Set the button click listener to remove the image
            deselectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Remove image and button on deselection
                    imageUrls.remove(imageUrl);
                    imageFrameLayout.removeView(imageView);
                    imageFrameLayout.removeView(deselectButton);
                    if (imageUrls.isEmpty() && videoUrls.isEmpty()) {
                        mediaContainer.setBackground(getResources().getDrawable(R.drawable.upload));
                        MemoryContainer.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                openPicker();
                            }
                        });
                    }
                }
            });

            imageFrameLayout.addView(imageView);
            imageFrameLayout.addView(deselectButton);

            currentRowLayout.addView(imageFrameLayout);

            count++;
        }

        // Display videos
        for (String videoUrl : videoUrls) {
            if (count % 3 == 0) {
                currentRowLayout = new LinearLayout(this);
                currentRowLayout.setLayoutParams(containerParams);
                currentRowLayout.setOrientation(LinearLayout.HORIZONTAL);
                verticalLayout.addView(currentRowLayout);
            }

            FrameLayout videoFrameLayout = new FrameLayout(this);
            videoFrameLayout.setLayoutParams(layoutParams);

            // Create a thumbnail for the video
            ImageView videoThumbnail = new ImageView(this);
            videoThumbnail.setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
            ));
            videoThumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);

            // Load video thumbnail using Glide
            Glide.with(this)
                    .load(videoUrl)
                    .override(250, 250)
                    .centerCrop()
                    .into(videoThumbnail);

            Button deselectButton = new Button(this);
            FrameLayout.LayoutParams buttonLayoutParams = new FrameLayout.LayoutParams(
                    80,
                    80
            );
            buttonLayoutParams.setMargins(0, 0, 0, 0);
            deselectButton.setPadding(0, 5, 0, 0);
            deselectButton.setLayoutParams(buttonLayoutParams);
            deselectButton.setText("X");

            deselectButton.setTextColor(getResources().getColor(R.color.black));
            deselectButton.setBackgroundColor(getResources().getColor(R.color.red));
            deselectButton.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

            // Set the button click listener to remove the video
            deselectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Remove video and button on deselection
                    videoUrls.remove(videoUrl);
                    videoFrameLayout.removeView(videoThumbnail);
                    videoFrameLayout.removeView(deselectButton);
                    if (imageUrls.isEmpty() && videoUrls.isEmpty()) {
                        mediaContainer.setBackground(getResources().getDrawable(R.drawable.upload));
                        MemoryContainer.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                openPicker();
                            }
                        });
                    }
                }
            });

            videoFrameLayout.addView(videoThumbnail);
            videoFrameLayout.addView(deselectButton);

            currentRowLayout.addView(videoFrameLayout);

            count++;
        }

        if (imageUrls.isEmpty() && videoUrls.isEmpty()) {
            mediaContainer.setBackground(getResources().getDrawable(R.drawable.upload));
        } else {
            mediaContainer.setBackground(null);
        }
    }

    private void uploadMediaToStorage(Uri mediaUri, String type) {
        // Show progress dialog while uploading
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Get Firebase storage reference
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef;
        if (type.equals("image")) {
            storageRef = storage.getReference().child("Memories/Images/" + System.currentTimeMillis() + ".jpg");
        } else {
            storageRef = storage.getReference().child("Memories/Videos/" + System.currentTimeMillis() + ".mp4");
        }

        // Upload media to storage
        storageRef.putFile(mediaUri)
                .addOnSuccessListener(taskSnapshot -> {
                    progressDialog.dismiss();
                    // Get the download URL of the uploaded media
                    Task<Uri> downloadUriTask = storageRef.getDownloadUrl();
                    downloadUriTask.addOnSuccessListener(uri -> {
                        String mediaUrl = uri.toString();
                        if (type.equals("image")) {
                            imageUrls.add(mediaUrl);
                        } else {
                            Videos.add(mediaUrl);
                        }
                        displaySelectedMedia(imageUrls, Videos);
                    });
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(UploadMemoryActivity.this, "Failed to upload " + type + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
