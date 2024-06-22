package com.example.yalatour.Classes;

public class Comment {
    private String CommentId;
    private String username;
    private String postId;
    private String commentDate;
    private String commentDescription;
    private String profileImageUrl;

    // Default constructor required for Firebase
    public Comment() {}

    public Comment(String commentId, String username, String postId, String commentDate, String commentDescription,String profileImageUrl ) {
        this.CommentId = commentId;
        this.username = username;
        this.postId = postId;
        this.commentDate = commentDate;
        this.commentDescription = commentDescription;
        this.profileImageUrl = profileImageUrl;
    }

    // Getters and Setters
    public String getCommentId() {
        return CommentId;
    }

    public void setCommentId(String CommentId) {
        this.CommentId = CommentId;
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

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getCommentDate() {
        return commentDate;
    }

    public void setCommentDate(String commentDate) {
        this.commentDate = commentDate;
    }

    public String getCommentDescription() {
        return commentDescription;
    }

    public void setCommentDescription(String commentDescription) {
        this.commentDescription = commentDescription;
    }
}