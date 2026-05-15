// model/Bid.java
package com.preloved_id.preloved.model;

import com.preloved_id.preloved.model.enums.BidStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bids", indexes = {
        @Index(name = "idx_product_status", columnList = "product_id, status"),
        @Index(name = "idx_buyer_status", columnList = "buyer_id, status"),
        @Index(name = "idx_expired_at", columnList = "expired_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double bidPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BidStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime expiredAt;

    private Double counterOfferPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // Buyer adalah User (abstract class) dengan inheritance JOINED
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        status = BidStatus.PENDING;
        expiredAt = createdAt.plusHours(48);
    }
}