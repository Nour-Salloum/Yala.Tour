package com.example.yalatour.Classes;

public class RatingClass {
    private String Ratingid;
    private String Rating_Placeid;
    private String Rating_userid;
    private Float Ratingvalue;

    public RatingClass() {
    }

    public RatingClass(String ratingid, String rating_Placeid, String rating_userid, Float ratingvalue) {
        Ratingid = ratingid;
        Rating_Placeid = rating_Placeid;
        Rating_userid = rating_userid;
        Ratingvalue = ratingvalue;
    }

    public String getRatingid() {
        return Ratingid;
    }

    public void setRatingid(String ratingid) {
        Ratingid = ratingid;
    }

    public String getRating_Placeid() {
        return Rating_Placeid;
    }

    public void setRating_Placeid(String rating_Placeid) {
        Rating_Placeid = rating_Placeid;
    }

    public String getRating_userid() {
        return Rating_userid;
    }

    public void setRating_userid(String rating_userid) {
        Rating_userid = rating_userid;
    }

    public Float getRatingvalue() {
        return Ratingvalue;
    }

    public void setRatingvalue(Float ratingvalue) {
        Ratingvalue = ratingvalue;
    }
}
