package com.example.yalatour.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.yalatour.Classes.TourismPlaceClass;
import com.example.yalatour.DetailsActivity.PlacesDetails;
import com.example.yalatour.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.ViewHolder> {

    private Context context;
    private List<TourismPlaceClass> favoritePlaces;

    public FavoritesAdapter(Context context, List<TourismPlaceClass> favoritePlaces) {
        this.context = context;
        this.favoritePlaces = favoritePlaces;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.favorite_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TourismPlaceClass place = favoritePlaces.get(position);

        holder.placeTitle.setText(place.getPlaceName());
        Glide.with(context).load(place.getPlaceImages().get(0)).into(holder.placeImage);

        holder.placeCard.setOnClickListener(v -> {
            Intent intent = new Intent(context, PlacesDetails.class);
            intent.putExtra("placeName", place.getPlaceName());
            intent.putExtra("placeDescription", place.getPlaceDescription());
            intent.putStringArrayListExtra("placeImages", new ArrayList<>(place.getPlaceImages()));
            intent.putExtra("TotalRating", place.getTotalRating());
            intent.putExtra("PlaceId", place.getPlaceId());
            context.startActivity(intent);
        });



        holder.removefavorite.setOnClickListener(v -> {
            removePlaceFromFavorite(place);
        });
    }

    @Override
    public int getItemCount() {
        return favoritePlaces.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView placeImage;
        TextView placeTitle;
        View placeCard;
        ImageButton removefavorite;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            placeImage = itemView.findViewById(R.id.placeImage);
            placeTitle = itemView.findViewById(R.id.placeTitle);
            placeCard = itemView.findViewById(R.id.placeCard);
            removefavorite = itemView.findViewById(R.id.RemoveFavorite);
        }
    }

    private void removePlaceFromFavorite(TourismPlaceClass place) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(context, "Please log in to remove from favorites", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = currentUser.getUid();
        DocumentReference favoriteRef = FirebaseFirestore.getInstance().collection("favorites").document(userId);

        favoriteRef.update("favoritePlaces", FieldValue.arrayRemove(place.getPlaceId()))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Removed from favorites", Toast.LENGTH_SHORT).show();
                    // Optionally, you can also remove the place from the local list to reflect the change immediately
                    favoritePlaces.remove(place);
                    notifyDataSetChanged();

                    // Check if the user has no favorite places left
                    if (favoritePlaces.isEmpty()) {
                        // Remove the entire user document from favorites collection
                        favoriteRef.delete()
                                .addOnSuccessListener(aVoid1 -> {
                                    // User document deleted successfully
                                    Toast.makeText(context, "User removed from favorites", Toast.LENGTH_SHORT).show();
                                    // Optionally, you can handle any UI updates here
                                })
                                .addOnFailureListener(e -> {
                                    // Failed to remove user document
                                    Toast.makeText(context, "Failed to remove user from favorites", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Failed to remove from favorites", Toast.LENGTH_SHORT).show());
    }


}
