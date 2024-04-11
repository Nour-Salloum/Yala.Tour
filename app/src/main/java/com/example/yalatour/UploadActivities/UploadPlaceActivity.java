package com.example.yalatour.UploadActivities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.yalatour.Activities.CityActivity;
import com.example.yalatour.Activities.TourismPlaces;
import com.example.yalatour.Adapters.CategoryAdapter;
import com.example.yalatour.Classes.Category;
import com.example.yalatour.Classes.TourismPlaceClass;
import com.example.yalatour.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UploadPlaceActivity extends AppCompatActivity {

    EditText uploadPlace, uploadDesc;
    Button saveButton;
    List<Uri> imageUris;
    FirebaseFirestore db;
    LinearLayout imagePicker;
    String cityName;
    RecyclerView categoryRecyclerView;
    CategoryAdapter categoryAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_place);

        uploadPlace = findViewById(R.id.uploadPlace);
        uploadDesc = findViewById(R.id.uploadPlaceDesc);
        saveButton = findViewById(R.id.savePlaceButton);
        db = FirebaseFirestore.getInstance();
        imageUris = new ArrayList<>();
        imagePicker = findViewById(R.id.imageContainer);
        categoryRecyclerView = findViewById(R.id.categoryRecyclerView);
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        cityName = getIntent().getStringExtra("cityName");

        // Populate categories in the RecyclerView
        populateCategories();

        imagePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });
    }

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

    private void openImagePicker() {
        Intent photoPicker = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        photoPicker.setType("image/*");
        photoPicker.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(photoPicker, 1);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 1) {
            if (data != null && data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri selectedImageUri = data.getClipData().getItemAt(i).getUri();
                    imageUris.add(selectedImageUri);
                }
                // Display selected images
                displaySelectedImages(imageUris);
                imagePicker.setBackground(null);
            }
        }

    }

    private void displaySelectedImages(List<Uri> imageUris) {
        LinearLayout imageContainer = findViewById(R.id.imageContainer);
        imageContainer.removeAllViews(); // Clear previous images if any

        // Create a new vertical LinearLayout to contain rows of images
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
        layoutParams.setMargins(10, 10, 20, 0); // Add margins between images

        int count = 0;
        LinearLayout currentRowLayout = null; // Initialize outside the loop
        for (Uri imageUri : imageUris) {
            if (count % 3 == 0) {
                // If count is divisible by 3, create a new horizontal LinearLayout
                currentRowLayout = new LinearLayout(this);
                currentRowLayout.setLayoutParams(containerParams);
                currentRowLayout.setOrientation(LinearLayout.HORIZONTAL);
                verticalLayout.addView(currentRowLayout);
            }

            // Create ImageView for the image
            ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(layoutParams);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP); // Adjust scale type as needed

            // Load image into ImageView using Glide
            Glide.with(this)
                    .load(imageUri)
                    .override(250, 250) // Set desired width and height
                    .centerCrop() // Scale type
                    .into(imageView);
            // Create a layout to hold the image and the deselect button
            LinearLayout imageLayout = new LinearLayout(this);
            imageLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            imageLayout.setOrientation(LinearLayout.VERTICAL); // Change orientation to VERTICAL
            currentRowLayout.addView(imageLayout);

            // Add the image to the current horizontal layout
            imageLayout.addView(imageView);

            // Create a button to deselect the image
            Button deselectButton = new Button(this);
            deselectButton.setText("X");
            deselectButton.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            deselectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Remove the corresponding image from the list of selected images
                    imageUris.remove(imageUri);
                    // Remove the image and the button from the parent layout
                    imageLayout.removeView(imageView);
                    imageLayout.removeView(deselectButton);
                    // Check if any image is selected
                    if (imageUris.isEmpty()) {
                        // If no image is selected, reset the background of the layout
                        imageContainer.setBackground(getResources().getDrawable(R.drawable.upload));
                    }
                }
            });
            imageLayout.addView(deselectButton);

            count++;
        }

        // If no image is selected, reset the background of the layout
        if (imageUris.isEmpty()) {
            imageContainer.setBackground(getResources().getDrawable(R.drawable.upload)); // Set your background drawable
        }
    }







    private void saveData() {
        String placeName = uploadPlace.getText().toString().trim();
        String placeDesc = uploadDesc.getText().toString().trim();
        List<String> selectedCategories = getSelectedCategories();

        if (placeName.isEmpty() || placeDesc.isEmpty() || selectedCategories.isEmpty() || imageUris.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields, select at least one category and choose at least one image", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Upload images to Firebase Storage
        uploadImagesToStorage(progressDialog, placeName, placeDesc, selectedCategories);
    }

    private void uploadImagesToStorage(final ProgressDialog progressDialog,
                                       final String placeName, final String placeDesc, final List<String> selectedCategories) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("TourismPlaces");
        List<Task<Uri>> uploadTasks = new ArrayList<>();
        for (Uri imageUri : imageUris) {
            final StorageReference imageRef = storageRef.child(imageUri.getLastPathSegment());
            UploadTask uploadTask = imageRef.putFile(imageUri);
            uploadTasks.add(uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return imageRef.getDownloadUrl();
                }
            }));
        }

        Task<List<Uri>> allTasks = Tasks.whenAllSuccess(uploadTasks);
        allTasks.addOnCompleteListener(new OnCompleteListener<List<Uri>>() {
            @Override
            public void onComplete(@NonNull Task<List<Uri>> task) {
                if (task.isSuccessful()) {
                    List<String> imageUrls = new ArrayList<>();
                    for (Uri uri : task.getResult()) {
                        imageUrls.add(uri.toString());
                    }
                    // Save data to Firestore
                    savePlaceData(imageUrls, progressDialog, placeName, placeDesc, selectedCategories);
                } else {

                    Toast.makeText(UploadPlaceActivity.this, "Failed to upload image(s)", Toast.LENGTH_SHORT).show();
                }
                progressDialog.dismiss();
            }
        });
    }

    private void savePlaceData(List<String> imageUrls, ProgressDialog progressDialog,
                               String placeName, String placeDesc, List<String> selectedCategories) {
        // Create a new document in the "TourismPlaces" collection with the provided data
        TourismPlaceClass place = new TourismPlaceClass(null, placeName, placeDesc, selectedCategories, imageUrls, cityName);
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
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    Toast.makeText(UploadPlaceActivity.this, "Failed to upload place: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                });
    }

}
