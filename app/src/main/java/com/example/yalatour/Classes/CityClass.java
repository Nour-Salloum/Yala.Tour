package com.example.yalatour.Classes;
public class CityClass {
    private String CityId;
    private String cityTitle;
    private String cityDesc;
    private String cityArea;
    private String cityImage;

    public String getCityTitle() {
        return cityTitle;
    }

    public String getCityDesc() {
        return cityDesc;
    }

    public String getCityArea() {
        return cityArea;
    }

    public String getCityImage() {
        return cityImage;
    }


    public void setCityId(String cityId) {
        CityId = cityId;
    }

    public void setCityTitle(String cityTitle) {
        this.cityTitle = cityTitle;
    }

    public void setCityDesc(String cityDesc) {
        this.cityDesc = cityDesc;
    }

    public void setCityArea(String cityArea) {
        this.cityArea = cityArea;
    }

    public void setCityImage(String cityImage) {
        this.cityImage = cityImage;
    }

    public String getCityId() {
        return CityId;
    }

    public CityClass(String cityId, String cityTitle, String cityDesc, String cityArea, String cityImage) {
        CityId = cityId;
        this.cityTitle = cityTitle;
        this.cityDesc = cityDesc;
        this.cityArea = cityArea;
        this.cityImage = cityImage;
    }

    public CityClass(){

    }
}
