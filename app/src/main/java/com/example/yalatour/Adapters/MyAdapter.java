package com.example.yalatour.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.yalatour.Classes.CityClass;
import com.example.yalatour.Classes.TourismPlaceClass;
import com.example.yalatour.DetailsActivity.DetailActivity;
import com.example.yalatour.EditActivities.EditCityActivity;
import com.example.yalatour.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private Context context;
    private List<CityClass> cityList;
    private FirebaseFirestore db;

    public MyAdapter(Context context, List<CityClass> cityList) {
        this.context = context;
        this.cityList = cityList;
        db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        CityClass city = cityList.get(position);
        Glide.with(context).load(city.getCityImage()).into(holder.recImage);
        holder.recTitle.setText(city.getCityTitle());
        holder.Area.setText(city.getCityArea());

        holder.recCard.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("Image", city.getCityImage());
            intent.putExtra("Description", city.getCityDesc());
            intent.putExtra("Title", city.getCityTitle());
            intent.putExtra("cityId", city.getCityId());
            context.startActivity(intent);
        });

        holder.EditCity.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditCityActivity.class);
            intent.putExtra("Image", city.getCityImage());
            intent.putExtra("Description", city.getCityDesc());
            intent.putExtra("Title", city.getCityTitle());
            intent.putExtra("Area", city.getCityArea());
            intent.putExtra("cityId", city.getCityId());
            context.startActivity(intent);
        });

        holder.DeleteCity.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage("Are you sure you want to delete this city?")
                    .setPositiveButton("Yes", (dialog, which) -> deleteCity(city, position))
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss());
            AlertDialog dialog = builder.create();
            dialog.show();
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
                            Boolean isAdmin = documentSnapshot.getBoolean("admin");

                            // Show or hide FAB based on admin status
                            if (isAdmin != null && isAdmin == true) {
                                holder.EditCity.setVisibility(View.VISIBLE);
                                holder.DeleteCity.setVisibility(View.VISIBLE);
                            } else {
                                holder.EditCity.setVisibility(View.GONE);
                                holder.DeleteCity.setVisibility(View.GONE);
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
        return cityList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView recImage;
        TextView recTitle, Area;
        RelativeLayout recCard;
        ImageButton EditCity, DeleteCity;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            recImage = itemView.findViewById(R.id.recImage);
            recCard = itemView.findViewById(R.id.recCard);
            Area = itemView.findViewById(R.id.Area);
            recTitle = itemView.findViewById(R.id.recTitle);
            EditCity = itemView.findViewById(R.id.EditCity);
            DeleteCity = itemView.findViewById(R.id.DeleteCity);
        }
    }

    private void deleteCity(CityClass city, int position) {
        db.collection("Cities").document(city.getCityId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    deleteCityPlaces(city.getCityTitle());
                    deleteImageFromStorage(city.getCityImage());
                    cityList.remove(position);
                    notifyItemRemoved(position);
                })
                .addOnFailureListener(e -> Log.e("MyAdapter", "Error deleting city document: " + e.getMessage(), e));
    }

    private void deleteCityPlaces(String cityId) {
        getPlacesForCity(cityId, placesList -> {
            TourismPlaceAdapter tourismPlaceAdapter = new TourismPlaceAdapter(context, placesList, cityId);
            for (TourismPlaceClass place : placesList) {
                tourismPlaceAdapter.deletePlaceFromFirestore(place);
            }
        });
    }

    private void deleteImageFromStorage(String imageUrl) {
        StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
        imageRef.delete()
                .addOnSuccessListener(aVoid -> Log.d("MyAdapter", "Image deleted successfully"))
                .addOnFailureListener(e -> Log.e("MyAdapter", "Error deleting image: " + e.getMessage(), e));
    }

    private void getPlacesForCity(String cityId, FirestoreCallback callback) {
        db.collection("TourismPlaces")
                .whereEqualTo("cityId", cityId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<TourismPlaceClass> placesList = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            TourismPlaceClass place = document.toObject(TourismPlaceClass.class);
                            placesList.add(place);
                        }
                        callback.onCallback(placesList);
                    } else {
                        Log.e("MyAdapter", "Error getting places: ", task.getException());
                    }
                });
    }

    private interface FirestoreCallback {
        void onCallback(List<TourismPlaceClass> placesList);
    }
}
