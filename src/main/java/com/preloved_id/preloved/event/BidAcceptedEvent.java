package com.preloved_id.preloved.event;

import com.preloved_id.preloved.model.Bid;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class BidAcceptedEvent extends ApplicationEvent {
    private final Bid bid;

    public BidAcceptedEvent(Object source, Bid bid) {
        super(source);
        this.bid = bid;
    }
}