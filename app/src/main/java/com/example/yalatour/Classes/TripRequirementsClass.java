package com.example.yalatour.Classes;

public class TripRequirementsClass {
    private String text;
    private boolean selected;

    public TripRequirementsClass(String text, boolean selected) {
        this.text = text;
        this.selected = selected;
    }

    public TripRequirementsClass() {
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

}
