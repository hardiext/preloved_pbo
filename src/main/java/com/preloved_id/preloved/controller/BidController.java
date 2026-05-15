package com.preloved_id.preloved.controller;

import com.preloved_id.preloved.dto.BidRequest;
import com.preloved_id.preloved.dto.BidResponse;
import com.preloved_id.preloved.dto.CounterOfferRequest;
import com.preloved_id.preloved.dto.BidMapper;
import com.preloved_id.preloved.model.Bid;
import com.preloved_id.preloved.service.BidService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bids")
@RequiredArgsConstructor
public class BidController {

    private final BidService bidService;
    private final BidMapper bidMapper;

    @PostMapping("/place")
    public ResponseEntity<BidResponse> placeBid(
            @Valid @RequestBody BidRequest request,
            Authentication authentication) {
        Bid bid = bidService.placeBid(
                request.getProductId(),
                authentication.getName(),
                request.getBidPrice());
        return ResponseEntity.ok(bidMapper.toResponse(bid));
    }

    @PreAuthorize("hasRole('SELLER')")
    @PostMapping("/{bidId}/accept")
    public ResponseEntity<BidResponse> acceptBid(
            @PathVariable Long bidId,
            Authentication authentication) {
        Bid bid = bidService.acceptBid(bidId, authentication.getName());
        return ResponseEntity.ok(bidMapper.toResponse(bid));
    }

    @PreAuthorize("hasRole('SELLER')")
    @PostMapping("/counter-offer")
    public ResponseEntity<BidResponse> counterOffer(
            @Valid @RequestBody CounterOfferRequest request,
            Authentication authentication) {
        Bid bid = bidService.counterOffer(request, authentication.getName());
        return ResponseEntity.ok(bidMapper.toResponse(bid));
    }

    @PostMapping("/{bidId}/cancel")
    public ResponseEntity<BidResponse> cancelBid(
            @PathVariable Long bidId,
            Authentication authentication) {
        Bid bid = bidService.cancelBid(bidId, authentication.getName());
        return ResponseEntity.ok(bidMapper.toResponse(bid));
    }

    @GetMapping("/product/{productId}/highest")
    public ResponseEntity<BidResponse> getHighestBid(@PathVariable Long productId) {
        Bid highestBid = bidService.getHighestBid(productId);
        return ResponseEntity.ok(bidMapper.toResponse(highestBid));
    }

    @GetMapping("/buyer/history")
    public ResponseEntity<Page<BidResponse>> getBuyerHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        Page<Bid> bids = bidService.getBuyerBidHistory(authentication.getName(), page, size);
        return ResponseEntity.ok(bids.map(bidMapper::toResponse));
    }

    @PreAuthorize("hasRole('SELLER')")
    @GetMapping("/seller/history")
    public ResponseEntity<Page<BidResponse>> getSellerHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        Page<Bid> bids = bidService.getSellerBidHistory(authentication.getName(), page, size);
        return ResponseEntity.ok(bids.map(bidMapper::toResponse));
    }
}