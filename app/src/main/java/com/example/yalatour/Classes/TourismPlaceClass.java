package com.example.yalatour.Classes;

import java.util.List;
import java.util.Objects;

public class TourismPlaceClass {
    private String placeId;
    private String placeName;
    private String placeDescription;
    private List<String> placeCategories;
    private List<String> placeImages;
    private String cityId;
    private float totalRating;


    public TourismPlaceClass() {
        // Required empty constructor for Firestore
    }

    public TourismPlaceClass(String placeId, String placeName, String placeDescription, List<String> placeCategories, List<String> placeImages, String cityId, float totalRating) {
        this.placeId = placeId;
        this.placeName = placeName;
        this.placeDescription = placeDescription;
        this.placeCategories = placeCategories;
        this.placeImages = placeImages;
        this.cityId = cityId;
        this.totalRating = totalRating;
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

    public String getCityId() {
        return cityId;
    }

    public void setCityId(String cityId) {
        this.cityId = cityId;
    }

    public float getTotalRating() {
        return totalRating;
    }

    public void setTotalRating(float totalRating) {
        this.totalRating = totalRating;
    }

    public String toString() {
        return "TourismPlaceClass{" +
                "placeId='" + placeId + '\'' +
                ", placeName='" + placeName + '\'' +
                ", cityId='" + cityId + '\'' +
                '}';
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TourismPlaceClass that = (TourismPlaceClass) o;
        return Objects.equals(placeId, that.placeId) &&
                Objects.equals(placeName, that.placeName) &&
                Objects.equals(cityId, that.cityId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(placeId, placeName, cityId);
    }
}