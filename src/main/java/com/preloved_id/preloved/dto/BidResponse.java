// dto/BidResponse.java
package com.preloved_id.preloved.dto;

import com.preloved_id.preloved.model.enums.BidStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class BidResponse {
    private Long id;
    private Double bidPrice;
    private BidStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime expiredAt;
    private Double counterOfferPrice;

    // Product info
    private Long productId;
    private String productName;
    private Double productPrice;

    // Buyer info (dari User abstract class)
    private String buyerId;
    private String buyerName; // dari getNama()
}