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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.yalatour.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class EditCityActivity extends AppCompatActivity {

    ImageView EditImage;
    Button SaveEdit, ChangeImage;
    EditText EditName, EditDesc, EditArea;
    Uri imageUri;
    String imageURL;
    String CityName, CityDesc, CityArea, CityId;

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_city);

        // Initialize views
        EditImage = findViewById(R.id.EditImage);
        SaveEdit = findViewById(R.id.SaveEdit);
        EditName = findViewById(R.id.EditName);
        EditDesc = findViewById(R.id.EditDesc);
        EditArea = findViewById(R.id.EditArea);
        ChangeImage = findViewById(R.id.ChangeImage);
        db = FirebaseFirestore.getInstance();

        // Retrieve intent data
        CityName = getIntent().getStringExtra("Title");
        CityDesc = getIntent().getStringExtra("Description");
        CityArea = getIntent().getStringExtra("Area");
        imageURL = getIntent().getStringExtra("Image");
        CityId = getIntent().getStringExtra("cityId");

        // Load image using Glide
        Glide.with(this).load(imageURL).into(EditImage);

        // Set current data to EditText fields
        EditName.setText(CityName);
        EditDesc.setText(CityDesc);
        EditArea.setText(CityArea);

        // Set click listener for changing image
        ChangeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker();
            }
        });

        // Set click listener for saving edited data
        SaveEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditCity();
            }
        });
    }

    // Method to open image picker
    private void openImagePicker() {
        Intent photoPicker = new Intent(Intent.ACTION_PICK);
        photoPicker.setType("image/*");
        startActivityForResult(photoPicker, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Handle result of image picker
        if (resultCode == RESULT_OK && requestCode == 1) {
            if (data != null) {
                // Get selected image URI and set it to ImageView
                imageUri = data.getData();
                EditImage.setImageURI(imageUri);
            } else {
                Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Method to edit city details
    private void EditCity() {
        // Retrieve updated data from EditText fields
        CityName = EditName.getText().toString();
        CityDesc = EditDesc.getText().toString();
        CityArea = EditArea.getText().toString();

        // Validate input fields
        if (CityName.isEmpty() || CityDesc.isEmpty() || CityArea.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress dialog during save process
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving changes...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Check if a new image is selected
        if (imageUri != null) {
            // Upload new image to Firebase Storage
            StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                    .child("CityImages")
                    .child(imageUri.getLastPathSegment());

            storageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                // If upload successful, get download URL
                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    imageURL = uri.toString();
                    // Update city data in Firestore with new image URL
                    db.collection("Cities").document(CityId)
                            .update("cityTitle", CityName, "cityDesc", CityDesc, "cityArea", CityArea, "cityImage", imageURL)
                            .addOnSuccessListener(aVoid -> {
                                progressDialog.dismiss();
                                Toast.makeText(this, "Changes saved successfully", Toast.LENGTH_SHORT).show();
                                // Return to previous activity with result indicating success
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("CityEdited", true);
                                setResult(RESULT_OK, resultIntent);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                Toast.makeText(this, "Failed to save changes", Toast.LENGTH_SHORT).show();
                                Log.e("EditCityActivity", "Failed to save changes", e);
                            });
                });
            }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                Log.e("EditCityActivity", "Failed to save changes", e);
            });
        } else {
            // Update city data in Firestore without changing the image
            db.collection("Cities").document(CityId)
                    .update("cityTitle", CityName, "cityDesc", CityDesc, "cityArea", CityArea)
                    .addOnSuccessListener(aVoid -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Changes saved successfully", Toast.LENGTH_SHORT).show();
                        // Return to previous activity with result indicating success
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("CityEdited", true);
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Failed to save changes", Toast.LENGTH_SHORT).show();
                        Log.e("EditCityActivity", "Failed to save changes", e);
                    });
        }
    }
}
