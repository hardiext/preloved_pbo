package com.preloved_id.preloved.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private Long id;
    private String name;
    private String description;
    private Double price;
    private String size;
    private String conditionProduct;
    private String imageUrl;
    private String category;
    private String status;
    private String sellerName;
}