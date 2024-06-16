package com.example.firestockbilly;

public class Category {
    private String name;

    public Category() {
        // Required empty public constructor for Firestore
    }

    public Category(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
