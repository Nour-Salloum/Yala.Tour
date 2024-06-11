package com.example.yalatour.Classes;

import java.util.List;

public class Favorite {
    private String userId;
    private List<TourismPlaceClass> favoritePlaces;

    public Favorite() {
    }

    public Favorite(String userId, List<TourismPlaceClass> favoritePlaces) {
        this.userId = userId;
        this.favoritePlaces = favoritePlaces;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<TourismPlaceClass> getFavoritePlaces() {
        return favoritePlaces;
    }

    public void setFavoritePlaces(List<TourismPlaceClass> favoritePlaces) {
        this.favoritePlaces = favoritePlaces;
    }

    public void addFavoritePlace(TourismPlaceClass place) {
        if (!favoritePlaces.contains(place)) {
            favoritePlaces.add(place);
        }
    }
}