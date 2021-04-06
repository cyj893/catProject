package com.example.catproject;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Info {

    public String catName;
    public String type;
    public String img;

    public Info() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Info(String catName, String type, String img) {
        this.catName = catName;
        this.type = type;
        this.img = img;
    }

    public String getCatName() {
        return catName;
    }

    public void setCatName(String catName) {
        this.catName = catName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getImg() { return img; }

    public void setImg(String img) { this.img = img; }

}
