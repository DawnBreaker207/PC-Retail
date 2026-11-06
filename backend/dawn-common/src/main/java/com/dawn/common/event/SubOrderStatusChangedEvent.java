package com.dawn.common.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SubOrderStatusChangedEvent(
        Long subOrderId,
        Long orderId,
        Long userId,
        String guestEmail,
        String newStatus,
        String trackingCode,
        int version) {

    public SubOrderStatusChangedEvent(Long subOrderId, Long orderId, Long userId,
                                       String guestEmail, String newStatus, String trackingCode) {
        this(subOrderId, orderId, userId, guestEmail, newStatus, trackingCode, 1);
    }
}
