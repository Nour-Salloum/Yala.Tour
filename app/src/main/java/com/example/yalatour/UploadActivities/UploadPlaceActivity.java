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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.yalatour.Adapters.CategoryAdapter;
import com.example.yalatour.Classes.Category;
import com.example.yalatour.Classes.TourismPlaceClass;
import com.example.yalatour.EditActivities.EditPlaceActivity;
import com.example.yalatour.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

// Define the UploadPlaceActivity class
public class UploadPlaceActivity extends AppCompatActivity {

    // Declare variables
    EditText uploadPlace, uploadDesc;
    Button saveButton;
    List<String> imageUrls;
    FirebaseFirestore db;
    LinearLayout imagePicker;
    String cityId;
    RecyclerView categoryRecyclerView;
    CategoryAdapter categoryAdapter;


    // Override onCreate method
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_place);

        // Initialize views and variables
        uploadPlace = findViewById(R.id.uploadPlace);
        uploadDesc = findViewById(R.id.uploadPlaceDesc);
        saveButton = findViewById(R.id.savePlaceButton);
        db = FirebaseFirestore.getInstance();
        imageUrls = new ArrayList<>();
        imagePicker = findViewById(R.id.imageContainer);
        categoryRecyclerView = findViewById(R.id.categoryRecyclerView);
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Get city id passed from previous activity
        cityId = getIntent().getStringExtra("cityId");

        // Populate categories in the RecyclerView
        populateCategories();

        // Set click listener for image picker
        imagePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker();
            }
        });

        // Set click listener for save button
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });
    }

    // Method to populate categories in the RecyclerView
    private void populateCategories() {
        // Define an array of categories
        String[] categories = {"Tourist Attractions", "Museums", "Religious Sites", "Activities", "Nature"};

        // Create a list of Category objects
        List<Category> categoryList = new ArrayList<>();
        for (String category : categories) {
            categoryList.add(new Category(category, false));
        }

        // Initialize the adapter with the list of categories
        categoryAdapter = new CategoryAdapter(categoryList);

        // Set the adapter for the RecyclerView
        categoryRecyclerView.setAdapter(categoryAdapter);
    }

    // Method to retrieve selected categories
    private List<String> getSelectedCategories() {
        List<String> selectedCategories = new ArrayList<>();
        for (Category category : categoryAdapter.getCategoryList()) {
            if (category.isSelected()) {
                selectedCategories.add(category.getName());
            }
        }
        return selectedCategories;
    }

    // Method to open image picker
    private void openImagePicker() {
        Intent photoPicker = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        photoPicker.setType("image/*");
        photoPicker.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(photoPicker, 1);
    }

    // Handle result from image picker
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            if (data != null) {

                if (data.getClipData() != null) {
                    // If multiple images are selected
                    int count = data.getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {
                        Uri imageUri = data.getClipData().getItemAt(i).getUri();
                        uploadImageToStorage(imageUri);
                    }
                } else if (data.getData() != null) {
                    // If single image is selected
                    Uri imageUri = data.getData();
                    uploadImageToStorage(imageUri);
                }
                displaySelectedImages(imageUrls);
                imagePicker.setBackground(null);
            }
        }
    }
    // Method to display selected images
    private void displaySelectedImages(List<String> imageUrls) {
        LinearLayout imageContainer = findViewById(R.id.imageContainer);
        imageContainer.removeAllViews();

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
        layoutParams.setMargins(10, 10, 20, 0);

        int count = 0;
        LinearLayout currentRowLayout = null;

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
                    80,
                    80
            );
            buttonLayoutParams.setMargins(0, 0, 0, 0);
            deselectButton.setPadding(0, 5, 0, 0);
            deselectButton.setLayoutParams(buttonLayoutParams);
            deselectButton.setText("X");

// Set button background and text color

            deselectButton.setTextColor(getResources().getColor(R.color.black)); // Set the text color
            deselectButton.setBackgroundColor(getResources().getColor(R.color.red)); // Set the background color
            deselectButton.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
// Set the button click listener to remove the image
            deselectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Remove image and button on deselection
                    imageUrls.remove(imageUrl);
                    imageFrameLayout.removeView(imageView);
                    imageFrameLayout.removeView(deselectButton);
                    if (imageUrls.isEmpty()) {
                        imageContainer.setBackground(getResources().getDrawable(R.drawable.upload));
                        imagePicker.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                openImagePicker();
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

        if (imageUrls.isEmpty()) {
            imageContainer.setBackground(getResources().getDrawable(R.drawable.upload));

        } else {
            imageContainer.setBackground(null);

        }
    }
    // Method to save data
    private void saveData() {
        String placeName = uploadPlace.getText().toString().trim();
        String placeDesc = uploadDesc.getText().toString().trim();
        List<String> selectedCategories = getSelectedCategories();

        if (placeName.isEmpty() || placeDesc.isEmpty() || selectedCategories.isEmpty() || imageUrls.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields, select at least one category and choose at least one image", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading...");
        progressDialog.setCancelable(false);
        progressDialog.show();


        savePlaceData(imageUrls,progressDialog, placeName, placeDesc, selectedCategories);
    }

    // Method to upload images to Firebase Storage
    private void uploadImageToStorage(Uri imageUri) {
        // Show progress dialog while uploading
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Get Firebase storage reference
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("TourismPlaces/" + System.currentTimeMillis() + ".jpg");
        // Upload image to storage
        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    progressDialog.dismiss();
                    // Get the download URL of the uploaded image
                    Task<Uri> downloadUriTask = storageRef.getDownloadUrl();
                    downloadUriTask.addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        imageUrls.add(imageUrl);
                        displaySelectedImages(imageUrls);
                    });
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(UploadPlaceActivity.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Method to save place data to Firestore
    private void savePlaceData(List<String> imageUrls, ProgressDialog progressDialog,
                               String placeName, String placeDesc, List<String> selectedCategories) {
        // Create a new document in the "TourismPlaces" collection with the provided data
        TourismPlaceClass place = new TourismPlaceClass(null, placeName, placeDesc, selectedCategories, imageUrls, cityId,0);
        db.collection("TourismPlaces")
                .add(place)
                .addOnSuccessListener(documentReference -> {
                    // Retrieve the document ID assigned by Firestore
                    String placeId = documentReference.getId();
                    // Set the placeId field of the place object
                    place.setPlaceId(placeId);
                    // Update the document with the placeId
                    documentReference.set(place)  // This will update the document with the placeId
                            .addOnSuccessListener(aVoid -> {
                                // Handle success
                                Toast.makeText(UploadPlaceActivity.this, "Place uploaded successfully", Toast.LENGTH_SHORT).show();
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("newPlaceAdded", true);
                                setResult(RESULT_OK, resultIntent);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                // Handle failure
                                Toast.makeText(UploadPlaceActivity.this, "Failed to upload place: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            });
                });

    }
}
