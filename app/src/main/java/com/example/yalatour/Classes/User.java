package com.example.yalatour.Classes;

import java.util.List;

public class User {
    private String username;
    private String email;
    private Boolean isUser;
    private List<String> fcmToken;

    public User() {
    }

    public User(String username, String email, Boolean isUser, List<String> fcmToken) {
        this.username = username;
        this.email = email;
        this.isUser = isUser;
        this.fcmToken = fcmToken;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getUser() {
        return isUser;
    }

    public void setUser(Boolean user) {
        isUser = user;
    }

    public List<String> getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(List<String> fcmToken) {
        this.fcmToken = fcmToken;
    }
}
