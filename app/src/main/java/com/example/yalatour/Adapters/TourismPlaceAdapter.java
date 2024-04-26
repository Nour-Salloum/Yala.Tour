package com.example.yalatour.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.yalatour.Classes.TourismPlaceClass;
import com.example.yalatour.DetailsActivity.PlacesDetails;
import com.example.yalatour.EditActivities.EditPlaceActivity;
import com.example.yalatour.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class TourismPlaceAdapter extends RecyclerView.Adapter<TourismPlaceAdapter.ViewHolder> {
    private Context context;
    private List<TourismPlaceClass> placeList;
    private String selectedCity;
    private FirebaseFirestore db;

    public TourismPlaceAdapter(Context context, List<TourismPlaceClass> placeList, String selectedCity) {
        this.context = context;
        this.placeList = placeList;
        this.selectedCity = selectedCity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tourism_place_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TourismPlaceClass place = placeList.get(position);
        if (place.getCityName().equals(selectedCity)) {
            holder.placeTitle.setText(place.getPlaceName());
            Glide.with(context).load(place.getPlaceImages().get(0)).into(holder.placeImage);
            holder.placeCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, PlacesDetails.class);
                    intent.putExtra("placeName", place.getPlaceName());
                    intent.putExtra("placeDescription", place.getPlaceDescription());
                    intent.putStringArrayListExtra("placeImages", new ArrayList<>(place.getPlaceImages()));
                    intent.putExtra("TotalRating",place.getTotalRating());
                    intent.putExtra("PlaceId", place.getPlaceId());
                    context.startActivity(intent);
                }
            });
        }
        holder.EditPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, EditPlaceActivity.class);
                intent.putExtra("placeName", place.getPlaceName());
                intent.putExtra("placeDescription", place.getPlaceDescription());
                intent.putStringArrayListExtra("placeImages", new ArrayList<>(place.getPlaceImages()));
                intent.putStringArrayListExtra("placeCategories", new ArrayList<>(place.getPlaceCategories()));
                intent.putExtra("PlaceId", place.getPlaceId());
                context.startActivity(intent);
            }
        });
        holder.Delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show a confirmation dialog before deleting the place
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Are you sure you want to delete this place?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Delete the place
                                deletePlace(place);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Do nothing, dismiss the dialog
                                dialog.dismiss();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();

            db.collection("Users").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String isAdmin = documentSnapshot.getString("isAdmin");
                            Log.d("UserAuthentication", "isAdmin value: " + isAdmin);

                            // Show or hide FAB based on admin status
                            if (isAdmin != null && isAdmin.equals("1")) {
                                holder.EditPlace.setVisibility(View.VISIBLE);
                                holder.Delete.setVisibility(View.VISIBLE);

                            } else {
                                holder.EditPlace.setVisibility(View.GONE);
                                holder.Delete.setVisibility(View.GONE);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("TourismPlaces", "Failed to fetch user data", e);
                    });
        } else {
            // User is not signed in, handle accordingly
        }

    }

    @Override
    public int getItemCount() {
        return placeList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView placeImage;
        TextView placeTitle;
        CardView placeCard;
        ImageButton EditPlace, Delete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            placeImage = itemView.findViewById(R.id.placeImage);
            placeTitle = itemView.findViewById(R.id.placeTitle);
            placeCard = itemView.findViewById(R.id.placeCard);
            EditPlace = itemView.findViewById(R.id.EditPlace);
            Delete = itemView.findViewById(R.id.DeletePlace);
        }
    }

    // Method to delete the place from Firestore and associated images from Firebase Storage
    private void deletePlace(TourismPlaceClass place) {
        // Get a reference to the Firestore document of the place
        DocumentReference placeRef = FirebaseFirestore.getInstance().collection("TourismPlaces").document(place.getPlaceId());

        // Get a reference to the images stored in Firebase Storage
        List<String> imageUrls = place.getPlaceImages();
        List<StorageReference> storageReferences = new ArrayList<>();
        for (String imageUrl : imageUrls) {
            // Convert image URL to StorageReference
            StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
            storageReferences.add(storageRef);
        }

        // Create a list to hold deletion tasks
        List<Task<Void>> deletionTasks = new ArrayList<>();

        // Delete images from Firebase Storage
        for (StorageReference storageRef : storageReferences) {
            Task<Void> deletionTask = storageRef.delete();
            deletionTasks.add(deletionTask);
        }

        // Wait for all deletion tasks to complete
        Task<Void> allDeletionTasks = Tasks.whenAll(deletionTasks);

        // Once all deletion tasks are completed, delete the document from Firestore
        allDeletionTasks.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // All images deleted successfully, now delete the document from Firestore
                placeRef.delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // Remove the deleted place from the placeList
                                placeList.remove(place);
                                // Notify the adapter that the dataset has changed
                                notifyDataSetChanged();
                                deleteReviews(place.getPlaceId());
                                deleteRatings(place.getPlaceId());
                                // Show toast message for successful deletion
                                Toast.makeText(context, "Place deleted successfully", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Show toast message for failure
                                Toast.makeText(context, "Failed to delete place: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Show toast message for failure
                Toast.makeText(context, "Failed to delete image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void deleteReviews(String placeId) {
        db = FirebaseFirestore.getInstance();
        db.collection("Reviews")
                .whereEqualTo("review_placeid", placeId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult().getDocuments()) {
                            String reviewId = document.getId();
                            db.collection("Reviews").document(reviewId).delete();
                        }
                    } else {
                        Log.e("PlacesDetails", "Error deleting reviews: ", task.getException());
                    }
                });
    }
    private void deleteRatings(String placeId) {
        db = FirebaseFirestore.getInstance();
        db.collection("Ratings")
                .whereEqualTo("rating_Placeid", placeId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult().getDocuments()) {
                            String ratingId = document.getId();
                            db.collection("Ratings").document(ratingId).delete();
                        }
                    } else {
                        Log.e("PlacesDetails", "Error deleting ratings: ", task.getException());
                    }
                });
    }




}
