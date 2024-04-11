package com.example.yalatour.Classes;

import java.util.List;

public class TourismPlaceClass {
    private String placeId; // New field
    private String placeName;
    private String placeDescription;
    private List<String> placeCategories;
    private List<String> placeImages;
    private String cityName;

    public TourismPlaceClass() {
        // Required empty constructor for Firestore
    }

    public TourismPlaceClass(String placeId, String placeName, String placeDescription, List<String> placeCategories, List<String> placeImages, String cityName) {
        this.placeId = placeId;
        this.placeName = placeName;
        this.placeDescription = placeDescription;
        this.placeCategories = placeCategories;
        this.placeImages = placeImages;
        this.cityName = cityName;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getPlaceDescription() {
        return placeDescription;
    }

    public void setPlaceDescription(String placeDescription) {
        this.placeDescription = placeDescription;
    }

    public List<String> getPlaceCategories() {
        return placeCategories;
    }

    public void setPlaceCategories(List<String> placeCategories) {
        this.placeCategories = placeCategories;
    }

    public List<String> getPlaceImages() {
        return placeImages;
    }

    public void setPlaceImages(List<String> placeImages) {
        this.placeImages = placeImages;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }
}
