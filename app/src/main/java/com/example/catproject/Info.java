package com.example.catproject;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Info {

    public String catName;
    public String type;

    public Info() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Info(String catName, String type) {
        this.catName = catName;
        this.type = type;
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

    @Override
    public String toString() {
        return "Info{" +
                "catName='" + catName + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
