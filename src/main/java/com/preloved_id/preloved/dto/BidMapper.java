// dto/BidMapper.java
package com.preloved_id.preloved.dto;

import com.preloved_id.preloved.model.Bid;
import org.springframework.stereotype.Component;

@Component
public class BidMapper {

    public BidResponse toResponse(Bid bid) {
        if (bid == null)
            return null;

        return BidResponse.builder()
                .id(bid.getId())
                .bidPrice(bid.getBidPrice())
                .status(bid.getStatus())
                .createdAt(bid.getCreatedAt())
                .expiredAt(bid.getExpiredAt())
                .counterOfferPrice(bid.getCounterOfferPrice())
                .productId(bid.getProduct().getId())
                .productName(bid.getProduct().getName())
                .productPrice(bid.getProduct().getPrice())
                .buyerId(bid.getBuyer().getId())
                .buyerName(bid.getBuyer().getNama()) // getNama() dari User
                .build();
    }
}