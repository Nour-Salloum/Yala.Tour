package com.example.yalatour.Classes;

import java.util.List;

public class Post {
    private String postId, userId, time, date, description, placename, username, profileImageUrl;
    private List<String> postImages; // Changed to List<String>
    private int numLikes;

    public Post() {
        // Required empty constructor
    }

    // Updated constructor to accept List<String> for postImages
    public Post(String postId, String userId, String time, String date, List<String> postImages, String description, String placename, String username, String profileImageUrl) {
        this.postId = postId;
        this.userId = userId;
        this.time = time;
        this.date = date;
        this.postImages = postImages;
        this.description = description;
        this.placename = placename;
        this.username = username;
        this.profileImageUrl = profileImageUrl;
        this.numLikes = 0;
    }

    // Getter and setter for postId, userId, time, date, description, placename, username, profileImageUrl

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<String> getPostImages() {
        return postImages;
    }

    public void setPostImages(List<String> postImages) {
        this.postImages = postImages;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPlacename() {
        return placename;
    }

    public void setPlacename(String placename) {
        this.placename = placename;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public int getNumLikes() {
        return numLikes;
    }

    public void setNumLikes(int numLikes) {
        this.numLikes = numLikes;
    }
}
