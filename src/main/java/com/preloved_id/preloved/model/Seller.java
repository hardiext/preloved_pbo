package com.preloved_id.preloved.model;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;

//entity digunakan untuk struktur program untuk match dengan db
@Entity
@PrimaryKeyJoinColumn(name = "user_id")
public class Seller extends User {

    private String storeName;

    private double totalSpent;

    public Seller() {
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public double getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(double totalSpent) {
        this.totalSpent = totalSpent;
    }

    @Override
    public void displayRole() {
        System.out.println("Role: Seller");
    }
}