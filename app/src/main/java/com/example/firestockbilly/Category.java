package com.example.firestockbilly;

import java.util.List;

public class Category {
    private String name;
    private List<String> accountIds;  // Änderung: Liste von accountIds

    public Category() {
        // Erforderlicher leerer öffentlicher Konstruktor für Firestore
    }

    public Category(String name, List<String> accountIds) {
        this.name = name;
        this.accountIds = accountIds;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getAccountIds() {
        return accountIds;
    }

    public void setAccountIds(List<String> accountIds) {
        this.accountIds = accountIds;
    }
}
