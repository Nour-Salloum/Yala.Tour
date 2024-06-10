package com.example.yalatour.Classes;

public class Post {
    public String postId, time, date, postimage, description, placename, username;

    public Post() {
        // Default constructor required for Firestore
    }

    public Post(String postId, String time, String date, String postimage, String description, String placename, String username) {
        this.postId = postId;
        this.time = time;
        this.date = date;
        this.postimage = postimage;
        this.description = description;
        this.placename = placename;
        this.username = username;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
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
}
