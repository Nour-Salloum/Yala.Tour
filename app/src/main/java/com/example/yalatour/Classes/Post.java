package com.example.yalatour.Classes;

public class Post {
    public String postId, userId, time, date, postimage, description, placename, username, profileImageUrl;

    public Post() {
    }

    public Post(String postId, String userId, String time, String date, String postimage, String description, String placename, String username, String profileImageUrl) {
        this.postId = postId;
        this.userId = userId;
        this.time = time;
        this.date = date;
        this.postimage = postimage;
        this.description = description;
        this.placename = placename;
        this.username = username;
        this.profileImageUrl = profileImageUrl;
    }

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

    public String getPostimage() {
        return postimage;
    }

    public void setPostimage(String postimage) {
        this.postimage = postimage;
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
}