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
import com.example.yalatour.Activities.TourismPlaces;
import com.example.yalatour.Classes.Favorite;
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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public TourismPlaceAdapter(Context context, List<TourismPlaceClass> placeList) {
        this.context = context;
        this.placeList = placeList;
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


        // Call checkIfPlaceIsFavorite synchronously before setting button click listeners
        checkIfPlaceIsFavorite(place, holder);


        holder.placeTitle.setText(place.getPlaceName());
        Glide.with(context).load(place.getPlaceImages().get(0)).into(holder.placeImage);

        holder.placeCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PlacesDetails.class);
                intent.putExtra("placeName", place.getPlaceName());
                intent.putExtra("placeDescription", place.getPlaceDescription());
                intent.putStringArrayListExtra("placeImages", new ArrayList<>(place.getPlaceImages()));
                intent.putExtra("TotalRating", place.getTotalRating());
                intent.putExtra("PlaceId", place.getPlaceId());
                context.startActivity(intent);
            }
        });

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
                            Boolean isUser = documentSnapshot.getBoolean("user");

                            // Show or hide FAB based on admin status
                            if (isUser != null && isUser == false) {
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

        // Handle the add to favorite button click

        holder.addfavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPlaceToFavorite(place, holder);
            }
        });

        holder.removefavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removePlaceFromFavorite(place, holder);
            }
        });


    }

    @Override
    public int getItemCount() {
        return placeList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView placeImage;
        TextView placeTitle;
        CardView placeCard;
        ImageButton EditPlace, Delete, addfavorite, removefavorite;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            placeImage = itemView.findViewById(R.id.placeImage);
            placeTitle = itemView.findViewById(R.id.placeTitle);
            placeCard = itemView.findViewById(R.id.placeCard);
            EditPlace = itemView.findViewById(R.id.EditPlace);
            Delete = itemView.findViewById(R.id.DeletePlace);
            addfavorite = itemView.findViewById(R.id.AddFavorite);
            removefavorite = itemView.findViewById(R.id.RemoveFavorite);
        }
    }

    private void addPlaceToFavorite(TourismPlaceClass place, ViewHolder holder) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(context, "Please log in to add to favorites", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = currentUser.getUid();
        DocumentReference favoriteRef = FirebaseFirestore.getInstance().collection("favorites").document(userId);

        favoriteRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // User's favorites exist, add the place
                favoriteRef.update("favoritePlaces", FieldValue.arrayUnion(place))
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(context, "Added to favorites", Toast.LENGTH_SHORT).show();
                            // Update button visibility
                            holder.addfavorite.setVisibility(View.GONE);
                            holder.removefavorite.setVisibility(View.VISIBLE);
                        })
                        .addOnFailureListener(e -> Toast.makeText(context, "Failed to add to favorites", Toast.LENGTH_SHORT).show());
            } else {
                // User's favorites do not exist, create new favorites document
                List<TourismPlaceClass> favoritePlaces = new ArrayList<>();
                favoritePlaces.add(place);
                Favorite favorite = new Favorite(userId, favoritePlaces);
                favoriteRef.set(favorite)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(context, "Added to favorites", Toast.LENGTH_SHORT).show();
                            // Update button visibility
                            holder.addfavorite.setVisibility(View.GONE);
                            holder.removefavorite.setVisibility(View.VISIBLE);
                        })
                        .addOnFailureListener(e -> Toast.makeText(context, "Failed to add to favorites", Toast.LENGTH_SHORT).show());
            }
        }).addOnFailureListener(e -> Toast.makeText(context, "Failed to access favorites", Toast.LENGTH_SHORT).show());
    }

    private void removePlaceFromFavorite(TourismPlaceClass place, ViewHolder holder) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(context, "Please log in to remove from favorites", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = currentUser.getUid();
        DocumentReference favoriteRef = FirebaseFirestore.getInstance().collection("favorites").document(userId);

        favoriteRef.update("favoritePlaces", FieldValue.arrayRemove(place))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Removed from favorites", Toast.LENGTH_SHORT).show();
                    // Update button visibility
                    holder.addfavorite.setVisibility(View.VISIBLE);
                    holder.removefavorite.setVisibility(View.GONE);

                    // Check if the user has no favorite places left
                    favoriteRef.get().addOnSuccessListener(documentSnapshot -> {
                        List<String> favoritePlaces = documentSnapshot.contains("favoritePlaces") ?
                                (List<String>) documentSnapshot.get("favoritePlaces") : new ArrayList<>();
                        if (favoritePlaces.isEmpty()) {
                            // If the user has no favorite places left, delete the user document
                            favoriteRef.delete()
                                    .addOnSuccessListener(aVoid1 -> {
                                        // User document deleted successfully
                                        Toast.makeText(context, "User removed from favorites", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        // Failed to remove user document
                                        Toast.makeText(context, "Failed to remove user from favorites", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    });
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Failed to remove from favorites", Toast.LENGTH_SHORT).show());
    }

    private void checkIfPlaceIsFavorite(TourismPlaceClass place, ViewHolder holder) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // User is not logged in, hide both buttons
            holder.addfavorite.setVisibility(View.GONE);
            holder.removefavorite.setVisibility(View.GONE);
            return;
        }
        String userId = currentUser.getUid();
        DocumentReference favoriteRef = FirebaseFirestore.getInstance().collection("favorites").document(userId);

        favoriteRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<TourismPlaceClass> favoritePlaces = documentSnapshot.toObject(Favorite.class).getFavoritePlaces();
                if (favoritePlaces.contains(place)) {
                    // Place is in favorites, show remove button and hide add button
                    holder.addfavorite.setVisibility(View.GONE);
                    holder.removefavorite.setVisibility(View.VISIBLE);
                } else {
                    // Place is not in favorites, show add button and hide remove button
                    holder.addfavorite.setVisibility(View.VISIBLE);
                    holder.removefavorite.setVisibility(View.GONE);
                }
            } else {
                // Favorites document does not exist, hide both buttons
                holder.addfavorite.setVisibility(View.VISIBLE);
                holder.removefavorite.setVisibility(View.GONE);
            }
        }).addOnFailureListener(e -> {
            // Failed to check favorites, hide both buttons
            holder.addfavorite.setVisibility(View.VISIBLE);
            holder.removefavorite.setVisibility(View.GONE);
        });
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
