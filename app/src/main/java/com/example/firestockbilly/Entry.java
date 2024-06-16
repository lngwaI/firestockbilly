package com.example.firestockbilly;

import java.util.List;

public class Entry {
    private String amount;
    private String category;
    private String categoryDetail;
    private List<String> paidForUserIds;

    public Entry(String amount, String category, String categoryDetail, List<String> paidForUserIds) {
        this.amount = amount;
        this.category = category;
        this.categoryDetail = categoryDetail;
        this.paidForUserIds = paidForUserIds;
    }

    // Getter und Setter

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategoryDetail() {
        return categoryDetail;
    }

    public void setCategoryDetail(String categoryDetail) {
        this.categoryDetail = categoryDetail;
    }

    public List<String> getPaidForUserIds() {
        return paidForUserIds;
    }

    public void setPaidForUserIds(List<String> paidForUserIds) {
        this.paidForUserIds = paidForUserIds;
    }
}
