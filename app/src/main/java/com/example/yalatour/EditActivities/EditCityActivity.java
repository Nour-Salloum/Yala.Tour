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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.yalatour.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class EditCityActivity extends AppCompatActivity {

    ImageView EditImage;
    Button SaveEdit,ChangeImage;
    EditText EditName, EditDesc, EditArea;
    Uri imageUri;
    String imageURL;
    String CityName, CityDesc, CityArea,CityId;

    FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_city);

        EditImage=findViewById(R.id.EditImage);
        SaveEdit=findViewById(R.id.SaveEdit);
        EditName=findViewById(R.id.EditName);
        EditDesc=findViewById(R.id.EditDesc);
        EditArea=findViewById(R.id.EditArea);
        ChangeImage=findViewById(R.id.ChangeImage);
        db=FirebaseFirestore.getInstance();

        CityName=getIntent().getStringExtra("Title");
        CityDesc=getIntent().getStringExtra("Description");
        CityArea=getIntent().getStringExtra("Area");
        imageURL=getIntent().getStringExtra("Image");
        CityId=getIntent().getStringExtra("Id");
        Glide.with(this).load(imageURL).into(EditImage);
        EditName.setText(CityName);
        EditDesc.setText(CityDesc);
        EditArea.setText(CityArea);
        ChangeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker();
            }
        });
        SaveEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditCity();
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
                EditImage.setImageURI(imageUri);
            } else {
                Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void  EditCity(){
        CityName=EditName.getText().toString();
        CityDesc=EditDesc.getText().toString();
        CityArea=EditArea.getText().toString();

        if (CityName.isEmpty() || CityDesc.isEmpty() || CityArea.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving changes...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        if (imageUri !=null){
            StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                    .child("CityImages")
                    .child(imageUri.getLastPathSegment());

            storageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    imageURL = uri.toString();
                    db.collection("Cities").document(CityId)
                            .update("cityTitle", CityName, "cityDesc", CityDesc, "cityArea", CityArea, "cityImage", imageURL)
                            .addOnSuccessListener(aVoid -> {
                                progressDialog.dismiss();
                                Toast.makeText(this, "Changes saved successfully", Toast.LENGTH_SHORT).show();
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("CityEdited", true);
                                setResult(RESULT_OK, resultIntent);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                Toast.makeText(this, "Failed to save changes", Toast.LENGTH_SHORT).show();
                            }
                            );
                });
            }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                Log.e("EditCityActivity", "Failed to save changes", e);
                });
        }
        else {
            db.collection("Cities").document(CityId)
                    .update("cityTitle", CityName, "cityDesc", CityDesc, "cityArea", CityArea)
                    .addOnSuccessListener(aVoid -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Changes saved successfully", Toast.LENGTH_SHORT).show();
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