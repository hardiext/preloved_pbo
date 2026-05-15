package com.preloved_id.preloved.service;

import com.preloved_id.preloved.model.Bid;
import com.preloved_id.preloved.model.enums.BidStatus;
import com.preloved_id.preloved.repository.BidRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BidCacheService {

    private final BidRepository bidRepository;

    @Cacheable(value = "highestBid", key = "#productId")
    public Bid getHighestBidWithCache(Long productId) {
        log.info("Cache MISS for productId={}, querying database...", productId);
        return bidRepository.findTopByProductIdAndStatusOrderByBidPriceDesc(productId, BidStatus.PENDING)
                .orElse(null);
    }

    @CacheEvict(value = "highestBid", key = "#productId")
    public void clearHighestBidCache(Long productId) {
        log.info("Cache EVICT for productId={}", productId);
    }
}