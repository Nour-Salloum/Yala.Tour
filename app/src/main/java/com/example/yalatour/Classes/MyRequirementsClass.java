package com.example.yalatour.Classes;

import java.util.List;

public class MyRequirementsClass {
    String MyRequirementsId;
    String Tripid;
    String Requrement_Userid;
    List<TripRequirementsClass> Requirements;

    public MyRequirementsClass() {
    }

    public MyRequirementsClass(String myRequirementsId, String tripid, String requrement_Userid, List<TripRequirementsClass> requirements) {
        MyRequirementsId = myRequirementsId;
        Tripid = tripid;
        Requrement_Userid = requrement_Userid;
        Requirements = requirements;
    }

    public String getMyRequirementsId() {
        return MyRequirementsId;
    }

    public void setMyRequirementsId(String myRequirementsId) {
        MyRequirementsId = myRequirementsId;
    }

    public String getTripid() {
        return Tripid;
    }

    public void setTripid(String tripid) {
        Tripid = tripid;
    }

    public String getRequrement_Userid() {
        return Requrement_Userid;
    }

    public void setRequrement_Userid(String requrement_Userid) {
        Requrement_Userid = requrement_Userid;
    }

    public List<TripRequirementsClass> getRequirements() {
        return Requirements;
    }

    public void setRequirements(List<TripRequirementsClass> requirements) {
        Requirements = requirements;
    }
}
