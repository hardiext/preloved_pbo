package com.preloved_id.preloved.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Double price;

    private String size;

    private String conditionProduct;

    private String imageUrl;

    private String category;

    private String status = "AVAILABLE";

    @ManyToOne
    @JoinColumn(name = "seller_id")
    private Seller seller;

    // OPTIMISTIC LOCKING
    @Version
    private Long version;
}