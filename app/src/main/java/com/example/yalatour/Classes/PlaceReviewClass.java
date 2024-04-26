package com.example.yalatour.Classes;



public class PlaceReviewClass
{
    private String ReviewId;
    private String username;
    private  String Review_placeid;
    private String Review_Date;
    private String Review_Description;
    public PlaceReviewClass() {
    }

    public PlaceReviewClass(String reviewId, String username, String review_placeid, String review_Date, String review_Description) {
        ReviewId = reviewId;
        this.username = username;
        Review_placeid = review_placeid;
        Review_Date = review_Date;
        Review_Description = review_Description;
    }

    public String getReviewId() {
        return ReviewId;
    }

    public void setReviewId(String reviewId) {
        ReviewId = reviewId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getReview_placeid() {
        return Review_placeid;
    }

    public void setReview_placeid(String review_placeid) {
        Review_placeid = review_placeid;
    }

    public String getReview_Date() {
        return Review_Date;
    }

    public void setReview_Date(String review_Date) {
        Review_Date = review_Date;
    }

    public String getReview_Description() {
        return Review_Description;
    }

    public void setReview_Description(String review_Description) {
        Review_Description = review_Description;
    }
}
