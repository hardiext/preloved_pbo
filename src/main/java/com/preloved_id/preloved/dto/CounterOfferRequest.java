package com.preloved_id.preloved.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CounterOfferRequest {
    @NotNull
    private Long bidId;

    @NotNull
    @Positive
    private Double counterPrice;
}