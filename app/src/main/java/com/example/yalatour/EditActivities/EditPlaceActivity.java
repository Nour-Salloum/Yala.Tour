package com.example.yalatour.EditActivities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.yalatour.Adapters.CategoryAdapter;
import com.example.yalatour.Classes.Category;
import com.example.yalatour.R;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

// Define the EditPlaceActivity class
public class EditPlaceActivity extends AppCompatActivity {

    // Declare variables
    EditText PlaceName, PlaceDesc;
    Button Edit;
    List<String> imageUrls;
    FirebaseFirestore db;
    LinearLayout Edit_imagePicker;
    RecyclerView categoryRecyclerView;
    CategoryAdapter categoryAdapter;
    LinearLayout imageContainer;
    TextView MoreImages;

    // List to keep track of images to delete
    List<String> imagesToDelete = new ArrayList<>();

    // Override onCreate method
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_place);

        // Initialize views
        PlaceName = findViewById(R.id.EditPlaceName);
        PlaceDesc = findViewById(R.id.EditPlaceDesc);
        Edit_imagePicker = findViewById(R.id.Edit_imageContainer);
        MoreImages = findViewById(R.id.MoreImages);
        Edit = findViewById(R.id.EditPlaceButton);

        // Set click listener for image picker
        MoreImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker();
            }
        });

        // Set click listener for edit button
        Edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveEditedData();
            }
        });

        // Retrieve data passed from previous activity
        String placeName = getIntent().getStringExtra("placeName");
        String placeDescription = getIntent().getStringExtra("placeDescription");
        List<String> placeImages = getIntent().getStringArrayListExtra("placeImages");
        List<String> placeCategories = getIntent().getStringArrayListExtra("placeCategories");

        // Set text for place name and description
        PlaceName.setText(placeName);
        PlaceDesc.setText(placeDescription);

        // Initialize imageUrls list
        imageUrls = new ArrayList<>();
        if (placeImages != null) {
            imageUrls.addAll(placeImages);
        }

        // Display selected images
        displaySelectedImages(imageUrls);

        // Initialize and populate category RecyclerView
        categoryRecyclerView = findViewById(R.id.categoryRecyclerView);
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        populateCategories(placeCategories);
    }

    // Method to populate category list
    private void populateCategories(List<String> placeCategories) {
        // Define categories
        String[] categories = {"Tourist Attractions", "Museums", "Religious Sites", "Activities", "Nature"};

        // Create category list with isSelected status based on placeCategories
        List<Category> categoryList = new ArrayList<>();
        for (String category : categories) {
            boolean isSelected = placeCategories != null && placeCategories.contains(category);
            categoryList.add(new Category(category, isSelected));
        }

        // Set up category adapter
        categoryAdapter = new CategoryAdapter(categoryList);
        categoryRecyclerView.setAdapter(categoryAdapter);
    }

    // Method to get selected categories
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
        // Create intent to pick images from gallery
        Intent photoPicker = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        photoPicker.setType("image/*");
        photoPicker.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(photoPicker, 1);
    }

    // Override onActivityResult method
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
            }
        }
    }

    // Method to upload image to storage
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
                    Toast.makeText(EditPlaceActivity.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Method to display selected images
    private void displaySelectedImages(List<String> imageUrls) {
        imageContainer = findViewById(R.id.Edit_imageContainer);
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
                        Edit_imagePicker.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                openImagePicker();
                            }
                        });
                        MoreImages.setVisibility(View.GONE);
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
            MoreImages.setVisibility(View.GONE);
        } else {
            imageContainer.setBackground(null);
            MoreImages.setVisibility(View.VISIBLE);
        }
    }

    // Method to save edited data
    private void saveEditedData() {
        String editedPlaceName = PlaceName.getText().toString().trim();
        String editedPlaceDesc = PlaceDesc.getText().toString().trim();
        List<String> editedCategories = getSelectedCategories();

        // Check if all fields are filled
        if (editedPlaceName.isEmpty() || editedPlaceDesc.isEmpty() || editedCategories.isEmpty() || imageUrls.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields and select at least one category and one image", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress dialog while updating
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Find images to delete
        List<String> originalImageUrls = getIntent().getStringArrayListExtra("placeImages");
        for (String originalUrl : originalImageUrls) {
            if (!imageUrls.contains(originalUrl)) {
                imagesToDelete.add(originalUrl);
            }
        }

        // Delete images from storage
        deleteImagesFromStorage(imagesToDelete);

        // Update place data
        updatePlaceData(editedPlaceName, editedPlaceDesc, editedCategories, progressDialog);
    }

    // Method to delete images from storage
    private void deleteImagesFromStorage(List<String> imagesToDelete) {
        for (String imageUrl : imagesToDelete) {
            StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
            imageRef.delete().addOnFailureListener(e -> {
                Log.e("Delete Image", "Failed to delete image: " + e.getMessage());
            });
        }
    }

    // Method to update place data
    private void updatePlaceData(String editedPlaceName, String editedPlaceDesc,
                                 List<String> editedCategories,
                                 ProgressDialog progressDialog) {
        String placeDocumentId = getIntent().getStringExtra("PlaceId");

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("TourismPlaces").document(placeDocumentId)
                .update("placeName", editedPlaceName,
                        "placeDescription", editedPlaceDesc,
                        "placeCategories", editedCategories,
                        "placeImages", imageUrls)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Toast.makeText(EditPlaceActivity.this, "Place updated successfully", Toast.LENGTH_SHORT).show();
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("placeEdited", true);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(EditPlaceActivity.this, "Failed to update data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
