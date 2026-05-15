// repository/BidRepository.java
package com.preloved_id.preloved.repository;

import com.preloved_id.preloved.model.Bid;
import com.preloved_id.preloved.model.enums.BidStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {

    Optional<Bid> findTopByProductIdAndStatusOrderByBidPriceDesc(Long productId, BidStatus status);

    List<Bid> findByProductIdOrderByBidPriceDesc(Long productId);

    // Buyer ID adalah String (dari User abstract class)
    Page<Bid> findByBuyerIdOrderByCreatedAtDesc(String buyerId, Pageable pageable);

    // Seller ID dari Product → Product.seller.user.id
    @Query("SELECT b FROM Bid b WHERE b.product.seller.user.id = :sellerId ORDER BY b.createdAt DESC")
    Page<Bid> findBySellerIdOrderByCreatedAtDesc(@Param("sellerId") String sellerId, Pageable pageable);

    boolean existsByProductIdAndBuyerIdAndStatus(Long productId, String buyerId, BidStatus status);

    long countByProductIdAndBuyerIdAndStatus(Long productId, String buyerId, BidStatus status);

    @Modifying
    @Query("UPDATE Bid b SET b.status = 'EXPIRED' WHERE b.status = 'PENDING' AND b.expiredAt < :now")
    int expireOldBids(@Param("now") LocalDateTime now);
}