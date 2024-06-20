package com.example.firestockbilly;

import java.util.List;

public class Category {
    private String name;
    private String userId;

    public Category() {
        // Erforderlicher leerer öffentlicher Konstruktor für Firestore
    }

    public Category(String name, String userId) {
        this.name = name;
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}

