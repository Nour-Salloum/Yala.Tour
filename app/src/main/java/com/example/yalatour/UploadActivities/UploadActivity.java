package com.example.yalatour.UploadActivities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.yalatour.Activities.CityActivity;
import com.example.yalatour.Classes.CityClass;
import com.example.yalatour.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class UploadActivity extends AppCompatActivity {

    ImageView uploadImage;
    Button saveButton;
    EditText uploadCity, uploadDesc, uploadCat;
    Uri imageUri;
    String imageURL;

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        uploadImage = findViewById(R.id.uploadImage);
        uploadDesc = findViewById(R.id.uploadDesc);
        uploadCity = findViewById(R.id.uploadCity);
        uploadCat = findViewById(R.id.uploadCat);
        saveButton = findViewById(R.id.saveButton);

        db = FirebaseFirestore.getInstance();

        uploadImage.setOnClickListener(new View.OnClickListener() {
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

    private void openImagePicker() {
        Intent photoPicker = new Intent(Intent.ACTION_PICK);
        photoPicker.setType("image/*");
        startActivityForResult(photoPicker, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 1) {
            if (data != null) {
                imageUri = data.getData();
                uploadImage.setImageURI(imageUri);
            } else {
                Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveData() {
        if (imageUri == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        String city = uploadCity.getText().toString().trim();
        String desc = uploadDesc.getText().toString().trim();
        String cat = uploadCat.getText().toString().trim();

        if (city.isEmpty() || desc.isEmpty() || cat.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child("CityImages")
                .child(imageUri.getLastPathSegment());

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        imageURL = uri.toString();
                        CityClass cityClass = new CityClass(city, desc, cat, imageURL);
                        uploadCityData(city, cityClass);
                        progressDialog.dismiss();
                    }).addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(UploadActivity.this, "Failed to get image URL", Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(UploadActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void uploadCityData(String city, CityClass cityClass) {
        db.collection("Cities").document(city)
                .set(cityClass)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(UploadActivity.this, "Data uploaded successfully", Toast.LENGTH_SHORT).show();
                    // Refresh the city list in CityActivity
                    Intent intent = new Intent(UploadActivity.this, CityActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(UploadActivity.this, "Failed to upload data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}
