package com.preloved_id.preloved.listener;

import com.preloved_id.preloved.event.BidAcceptedEvent;
import com.preloved_id.preloved.event.BidPlacedEvent;
import com.preloved_id.preloved.model.Bid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    @Async
    @EventListener
    public void handleBidPlaced(BidPlacedEvent event) {
        Bid bid = event.getBid();
        log.info("Event: Bid placed - product={}, buyer={}, price={}",
                bid.getProduct().getId(), bid.getBuyer().getEmail(), bid.getBidPrice());

    }

    @Async
    @EventListener
    public void handleBidAccepted(BidAcceptedEvent event) {
        Bid bid = event.getBid();
        log.info("Event: Bid accepted - product={}, buyer={}",
                bid.getProduct().getId(), bid.getBuyer().getEmail());

    }
}