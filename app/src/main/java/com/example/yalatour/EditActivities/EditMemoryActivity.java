package com.example.yalatour.EditActivities;

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

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.yalatour.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class EditMemoryActivity extends AppCompatActivity {

    EditText MemoryEditText;
    Button EditsaveMemoryButton;
    ImageButton EditAddText;
    List<String> Edit_imageUrls;
    List<String> Edit_Videos;
    List<String> Edit_Texts;
    String Edit_memoryId;

    FirebaseFirestore db;
    FirebaseStorage storage;
    LinearLayout MemoryEditContainer, EditTextLayout, EditdynamicTextContainer;
    List<String> UpdatedImageUrls = new ArrayList<>();
    List<String> UpdatedVideoUrls = new ArrayList<>();
    List<String> UpdatedTexts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_memory);

        MemoryEditText = findViewById(R.id.MemoryEditText);
        EditsaveMemoryButton = findViewById(R.id.EditsaveMemoryButton);
        EditAddText = findViewById(R.id.EditAddText);
        MemoryEditContainer = findViewById(R.id.MemoryEditContainer);
        EditTextLayout = findViewById(R.id.EditTextLayout);
        EditdynamicTextContainer = findViewById(R.id.EditdynamicTextContainer);
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance(); // Initialize Firebase Storage
        Edit_memoryId = getIntent().getStringExtra("MemoryId");
        Edit_Texts = new ArrayList<>();
        Edit_imageUrls = new ArrayList<>();
        Edit_Videos = new ArrayList<>();
        UpdatedImageUrls= new ArrayList<>();
        UpdatedVideoUrls = new ArrayList<>();
        UpdatedTexts = new ArrayList<>();
        fetchMemoryDetails(Edit_memoryId);

        EditsaveMemoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.collection("Memories").document(Edit_memoryId)
                        .update("memory_Texts", getNonEmptyNewTexts(),
                                "memory_Images", UpdatedImageUrls,
                                "memory_Videos", UpdatedVideoUrls)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(EditMemoryActivity.this, "Memory updated successfully", Toast.LENGTH_SHORT).show();
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("MemoryEdited", true);
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(EditMemoryActivity.this, "Failed to update memory", Toast.LENGTH_SHORT).show();
                        });


            }
        });

        EditAddText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewMemoryText();
            }
        });

    }

    private void fetchMemoryDetails(String memoryId) {
        db.collection("Memories").document(memoryId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Retrieve memory data
                        Edit_Texts = (List<String>) documentSnapshot.get("memory_Texts");
                        Edit_imageUrls = (List<String>) documentSnapshot.get("memory_Images");
                        Edit_Videos = (List<String>) documentSnapshot.get("memory_Videos");
                        UpdatedImageUrls.addAll(Edit_imageUrls);
                        UpdatedVideoUrls.addAll(Edit_Videos);
                        UpdatedTexts.addAll(Edit_Texts);
                        displayTexts(UpdatedTexts);
                        Edit_displaySelectedMedia(UpdatedImageUrls, UpdatedVideoUrls);
                    } else {
                        Toast.makeText(EditMemoryActivity.this, "Memory not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EditMemoryActivity.this, "Failed to fetch memory details", Toast.LENGTH_SHORT).show();
                });
    }

    private void displayTexts(List<String> texts) {
        for (String text : texts) {
            EditText editText = new EditText(this);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    815,
                    170
            );
            layoutParams.setMargins(0, 15, 0, 0);
            editText.setLayoutParams(layoutParams);
            editText.setText(text);
            editText.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            editText.setBackground(ContextCompat.getDrawable(this, R.drawable.lavender_boarder));
            editText.setPadding(16, 16, 16, 16);
            EditdynamicTextContainer.addView(editText);
        }
    }

    private void addNewMemoryText() {
        EditText newMemoryText = new EditText(this);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                815, // Width in pixels
                170  // Height in pixels
        );
        layoutParams.setMargins(0, 15, 0, 0);
        newMemoryText.setLayoutParams(layoutParams);
        newMemoryText.setHint("Enter Text");
        newMemoryText.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        newMemoryText.setBackground(ContextCompat.getDrawable(this, R.drawable.lavender_boarder));
        newMemoryText.setPadding(16, 16, 16, 16);
        EditdynamicTextContainer.addView(newMemoryText);
    }

    private List<String> getNonEmptyNewTexts() {
        List<String> newTexts = new ArrayList<>();
        for (int i = 0; i < EditdynamicTextContainer.getChildCount(); i++) {
            View view = EditdynamicTextContainer.getChildAt(i);
            if (view instanceof EditText) {
                String text = ((EditText) view).getText().toString().trim();
                if (!text.isEmpty()) {
                    newTexts.add(text);
                }
            }
        }
        return newTexts;
    }


    private void openPicker() {
        Intent mediaPicker = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        mediaPicker.setType("*/*");
        String[] mimeTypes = {"image/*", "video/*"};
        mediaPicker.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        mediaPicker.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(mediaPicker, 1);
    }


    private void uploadImageAndVideoToStorage(Uri fileUri) {
        // Show progress dialog while uploading
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Get Firebase storage reference
        FirebaseStorage storage = FirebaseStorage.getInstance();
        String fileType = getContentResolver().getType(fileUri);
        String folder = fileType.startsWith("image/") ? "images/" : "videos/";
        StorageReference storageRef = storage.getReference().child("TourismPlaces/" + folder + System.currentTimeMillis());

        // Upload file to storage
        storageRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> {
                    progressDialog.dismiss();
                    // Get the download URL of the uploaded file
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String fileUrl = uri.toString();
                        if (folder.equals("images/")) {
                            UpdatedImageUrls.add(fileUrl);
                        } else if (folder.equals("videos/")) {
                            UpdatedVideoUrls.add(fileUrl);
                        }
                        Edit_displaySelectedMedia(UpdatedImageUrls, UpdatedVideoUrls);
                    });
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(EditMemoryActivity.this, "Failed to upload file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Method to handle media picker result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            List<Uri> newFileUris = new ArrayList<>();
            if (data.getClipData() != null) {
                for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                    Uri fileUri = data.getClipData().getItemAt(i).getUri();
                    newFileUris.add(fileUri);
                }
            } else if (data.getData() != null) {
                Uri fileUri = data.getData();
                newFileUris.add(fileUri);
            }

            // Upload only the new files
            for (Uri fileUri : newFileUris) {
                uploadImageAndVideoToStorage(fileUri);
            }
        }
    }
    private void Edit_displaySelectedMedia(List<String> imageUrls, List<String> videoUrls) {
        LinearLayout mediaContainer = findViewById(R.id.MemoryEditContainer);
        if (mediaContainer != null) {
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

                // Create the "X" button with custom margins
                Button deselectButton = new Button(this);
                FrameLayout.LayoutParams buttonLayoutParams = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                );
                buttonLayoutParams.setMargins(10, -20, 0, 0); // Adjust margins as needed
                deselectButton.setLayoutParams(buttonLayoutParams);

                deselectButton.setText("X");

                // Set button background and position
                deselectButton.setBackgroundResource(R.drawable.styles);
                deselectButton.setGravity(Gravity.LEFT | Gravity.TOP);

                // Set the button click listener to remove the image
                deselectButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Remove image and button on deselection
                        imageUrls.remove(imageUrl);
                        deleteFileFromStorage(imageUrl); // Delete the image from Firebase Storage
                        imageFrameLayout.removeView(imageView);
                        imageFrameLayout.removeView(deselectButton);
                        if (imageUrls.isEmpty() && videoUrls.isEmpty()) {
                            mediaContainer.setBackground(getResources().getDrawable(R.drawable.upload));
                            MemoryEditContainer.setOnClickListener(new View.OnClickListener() {
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

                // Create the "X" button with custom margins
                Button deselectButton = new Button(this);
                FrameLayout.LayoutParams buttonLayoutParams = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                );
                buttonLayoutParams.setMargins(10, -20, 0, 0); // Adjust margins as needed
                deselectButton.setLayoutParams(buttonLayoutParams);

                deselectButton.setText("X");

                // Set button background and position
                deselectButton.setBackgroundResource(R.drawable.styles);
                deselectButton.setGravity(Gravity.LEFT | Gravity.TOP);

                // Set the button click listener to remove the video
                deselectButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Remove video and button on deselection
                        videoUrls.remove(videoUrl);
                        deleteFileFromStorage(videoUrl); // Delete the video from Firebase Storage
                        videoFrameLayout.removeView(videoThumbnail);
                        videoFrameLayout.removeView(deselectButton);
                        if (imageUrls.isEmpty() && videoUrls.isEmpty()) {
                            mediaContainer.setBackground(getResources().getDrawable(R.drawable.upload));
                            MemoryEditContainer.setOnClickListener(new View.OnClickListener() {
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
    }

    private void deleteFileFromStorage(String fileUrl) {
        // Create a reference to the file to delete
        StorageReference storageReference = storage.getReferenceFromUrl(fileUrl);

        // Delete the file
        storageReference.delete().addOnSuccessListener(aVoid -> {

        }).addOnFailureListener(exception -> {
            // An error occurred
            Toast.makeText(EditMemoryActivity.this, "Failed to delete file", Toast.LENGTH_SHORT).show();
        });
    }
}
