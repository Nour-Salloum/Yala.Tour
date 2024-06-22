package com.example.yalatour.Activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yalatour.Adapters.FavoritesAdapter;
import com.example.yalatour.Classes.TourismPlaceClass;
import com.example.yalatour.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FavoritePage extends AppCompatActivity {

    private RecyclerView favoriteRecyclerView;
    private FavoritesAdapter favoritesAdapter;
    private List<TourismPlaceClass> favoritePlaces;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        favoriteRecyclerView = findViewById(R.id.FavoriteRecyclerView);
        favoriteRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        favoritePlaces = new ArrayList<>();
        favoritesAdapter = new FavoritesAdapter(this, favoritePlaces);
        favoriteRecyclerView.setAdapter(favoritesAdapter);

        favoriteRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loadFavoritePlaces();
    }

    private void loadFavoritePlaces() {
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("favorites").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.d("FavoritePage", "DocumentSnapshot data: " + documentSnapshot.getData());
                        List<Map<String, Object>> favoriteList = (List<Map<String, Object>>) documentSnapshot.get("favoritePlaces");
                        if (favoriteList != null) {
                            for (Map<String, Object> placeMap : favoriteList) {
                                Log.d("FavoritePage", "PlaceMap: " + placeMap);
                                TourismPlaceClass place = new TourismPlaceClass();
                                place.setPlaceName((String) placeMap.get("placeName"));
                                place.setPlaceDescription((String) placeMap.get("placeDescription"));
                                place.setPlaceImages((List<String>) placeMap.get("placeImages"));
                                place.setTotalRating(((Double) placeMap.get("totalRating")).floatValue()); // Fixed casting issue
                                place.setPlaceId((String) placeMap.get("placeId"));
                                favoritePlaces.add(place);
                            }
                            favoritesAdapter.notifyDataSetChanged();
                        } else {
                            Log.d("FavoritePage", "Favorite list is null");
                        }
                    } else {
                        Log.d("FavoritePage", "No such document");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(FavoritePage.this, "Failed to load favorite places", Toast.LENGTH_SHORT).show();
                    Log.e("FavoritePage", "Error loading favorites", e);
                });
    }
}
