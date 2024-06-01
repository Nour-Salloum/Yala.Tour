package com.example.yalatour.Classes;

import com.bumptech.glide.request.Request;

import java.util.ArrayList;
import java.util.List;

public class TripClass {
    private String TripId;
    private String TripDate;
    private int NumberofDays;
    private List<TripRequirementsClass> Requirements;
    private List<TourismPlaceClass> TripPlaces;
    private List<TripRequestsClass> Requests;
    private List<String> Usersid;
    private String TripAdminid;
    private String Code;
    private String TripName;


    public TripClass() {
    }

    public TripClass(String tripId, String tripDate, int numberofDays, List<TripRequirementsClass> requirements, List<TourismPlaceClass> tripPlaces, List<TripRequestsClass> requests, List<String> usersid, String tripAdminid, String code, String tripName) {
        TripId = tripId;
        TripDate = tripDate;
        NumberofDays = numberofDays;
        Requirements = requirements;
        TripPlaces = tripPlaces;
        this.Requests = new ArrayList<>();
        Usersid = usersid;
        TripAdminid = tripAdminid;
        Code = code;
        TripName = tripName;
    }

    public String getTripId() {
        return TripId;
    }

    public void setTripId(String tripId) {
        TripId = tripId;
    }

    public String getTripDate() {
        return TripDate;
    }

    public void setTripDate(String tripDate) {
        TripDate = tripDate;
    }

    public int getNumberofDays() {
        return NumberofDays;
    }

    public void setNumberofDays(int numberofDays) {
        NumberofDays = numberofDays;
    }

    public List<TripRequirementsClass> getRequirements() {
        return Requirements;
    }

    public void setRequirements(List<TripRequirementsClass> requirements) {
        Requirements = requirements;
    }

    public List<TourismPlaceClass> getTripPlaces() {
        return TripPlaces;
    }

    public void setTripPlaces(List<TourismPlaceClass> tripPlaces) {
        TripPlaces = tripPlaces;
    }

    public List<TripRequestsClass> getRequests() {
        return Requests;
    }

    public void setRequests(List<TripRequestsClass> requests) {
        Requests = requests;
    }

    public List<String> getUsersid() {
        return Usersid;
    }

    public void setUsersid(List<String> usersid) {
        Usersid = usersid;
    }

    public String getTripAdminid() {
        return TripAdminid;
    }

    public void setTripAdminid(String tripAdminid) {
        TripAdminid = tripAdminid;
    }

    public String getCode() {
        return Code;
    }

    public void setCode(String code) {
        Code = code;
    }

    public String getTripName() {
        return TripName;
    }

    public void setTripName(String tripName) {
        TripName = tripName;
    }
}
