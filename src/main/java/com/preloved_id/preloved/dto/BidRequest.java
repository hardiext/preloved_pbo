package com.preloved_id.preloved.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class BidRequest {
    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Bid price is required")
    @Positive(message = "Bid price must be positive")
    private Double bidPrice;
}