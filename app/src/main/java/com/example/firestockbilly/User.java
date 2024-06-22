package com.example.firestockbilly;

public class User {
    private String name;
    private String id;
    private boolean isAdmin;

    public User() {
        // Leerer Konstruktor für Firebase
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User(String id, String name, boolean isAdmin) {
        this.name = name;
        this.id = id;
        this.isAdmin = isAdmin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }
}
