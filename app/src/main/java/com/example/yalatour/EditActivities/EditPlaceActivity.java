package com.example.yalatour.EditActivities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class EditPlaceActivity extends AppCompatActivity {

    EditText PlaceName, PlaceDesc;
    Button Edit;
    List<String> imageUrls;
    FirebaseFirestore db;
    LinearLayout Edit_imagePicker;
    RecyclerView categoryRecyclerView;
    CategoryAdapter categoryAdapter;
    LinearLayout imageContainer;

    // List to keep track of images to delete
    List<String> imagesToDelete = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_place);

        PlaceName = findViewById(R.id.EditPlaceName);
        PlaceDesc = findViewById(R.id.EditPlaceDesc);
        Edit_imagePicker = findViewById(R.id.Edit_imageContainer);
        Edit = findViewById(R.id.EditPlaceButton);

        Edit_imagePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker();
            }
        });

        Edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveEditedData();
            }
        });

        String placeName = getIntent().getStringExtra("placeName");
        String placeDescription = getIntent().getStringExtra("placeDescription");
        List<String> placeImages = getIntent().getStringArrayListExtra("placeImages");
        List<String> placeCategories = getIntent().getStringArrayListExtra("placeCategories");

        PlaceName.setText(placeName);
        PlaceDesc.setText(placeDescription);

        imageUrls = new ArrayList<>();
        if (placeImages != null) {
            imageUrls.addAll(placeImages);
        }

        displaySelectedImages(imageUrls);

        categoryRecyclerView = findViewById(R.id.categoryRecyclerView);
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        populateCategories(placeCategories);
    }

    private void populateCategories(List<String> placeCategories) {
        String[] categories = {"Tourist Attractions", "Museums", "Religious Sites", "Activities", "Nature"};

        List<Category> categoryList = new ArrayList<>();
        for (String category : categories) {
            boolean isSelected = placeCategories != null && placeCategories.contains(category);
            categoryList.add(new Category(category, isSelected));
        }

        categoryAdapter = new CategoryAdapter(categoryList);
        categoryRecyclerView.setAdapter(categoryAdapter);
    }

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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            if (data != null) {
                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {
                        Uri imageUri = data.getClipData().getItemAt(i).getUri();
                        uploadImageToStorage(imageUri);
                    }
                } else if (data.getData() != null) {
                    Uri imageUri = data.getData();
                    uploadImageToStorage(imageUri);
                }
            }
        }
    }

    private void uploadImageToStorage(Uri imageUri) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("TourismPlaces/" + System.currentTimeMillis() + ".jpg");
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

            ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(layoutParams);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            Glide.with(this)
                    .load(imageUrl)
                    .override(250, 250)
                    .centerCrop()
                    .into(imageView);

            LinearLayout imageLayout = new LinearLayout(this);
            imageLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            imageLayout.setOrientation(LinearLayout.VERTICAL);
            currentRowLayout.addView(imageLayout);
            imageLayout.addView(imageView);

            Button deselectButton = new Button(this);
            deselectButton.setText("X");
            deselectButton.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            deselectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    imageUrls.remove(imageUrl);
                    imageLayout.removeView(imageView);
                    imageLayout.removeView(deselectButton);
                    if (imageUrls.isEmpty()) {
                        imageContainer.setBackground(getResources().getDrawable(R.drawable.upload));
                    }
                }
            });
            imageLayout.addView(deselectButton);

            count++;
        }

        if (imageUrls.isEmpty()) {
            imageContainer.setBackground(getResources().getDrawable(R.drawable.upload));
        } else {
            imageContainer.setBackground(null); // Remove background
        }
    }

    private void saveEditedData() {
        String editedPlaceName = PlaceName.getText().toString().trim();
        String editedPlaceDesc = PlaceDesc.getText().toString().trim();
        List<String> editedCategories = getSelectedCategories();

        if (editedPlaceName.isEmpty() || editedPlaceDesc.isEmpty() || editedCategories.isEmpty() || imageUrls.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields and select at least one category and one image", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        List<String> originalImageUrls = getIntent().getStringArrayListExtra("placeImages");
        for (String originalUrl : originalImageUrls) {
            if (!imageUrls.contains(originalUrl)) {
                imagesToDelete.add(originalUrl);
            }
        }

        deleteImagesFromStorage(imagesToDelete);

        updatePlaceData(editedPlaceName, editedPlaceDesc, editedCategories, progressDialog);
    }

    private void deleteImagesFromStorage(List<String> imagesToDelete) {
        for (String imageUrl : imagesToDelete) {
            StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
            imageRef.delete().addOnFailureListener(e -> {
                Log.e("Delete Image", "Failed to delete image: " + e.getMessage());
            });
        }
    }

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
