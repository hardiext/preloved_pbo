package com.preloved_id.preloved.dto;

import lombok.Data;

@Data
public class ProductRequest {

    private String name;
    private String description;
    private Double price;
    private String size;
    private String conditionProduct;
    private String imageUrl;
    private String category;
}