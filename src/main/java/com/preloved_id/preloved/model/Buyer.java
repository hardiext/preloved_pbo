package com.preloved_id.preloved.model;

import jakarta.persistence.Entity;


//entity digunakan untuk struktur program untuk match dengan db
@Entity

//pewarisan dari user
public class Buyer extends User {

    private double totalSpent;

    public Buyer() {
    }

    public double getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(double totalSpent) {
        this.totalSpent = totalSpent;
    }

    @Override
    public void displayRole() {
        System.out.println("Role: Buyer");
    }
}