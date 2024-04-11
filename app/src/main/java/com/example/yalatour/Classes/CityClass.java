package com.example.yalatour.Classes;
public class CityClass {
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

    public CityClass(String cityTitle, String cityDesc, String cityArea, String cityImage){
        this.cityTitle = cityTitle;
        this.cityDesc = cityDesc;
        this.cityArea = cityArea;
        this.cityImage = cityImage;
    }

    public CityClass(){

    }
}
