package com.dawn.common.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PaymentFailedEvent(Long paymentId, Long orderId, String reason, int version) {
    public PaymentFailedEvent(Long paymentId, Long orderId, String reason) {
        this(paymentId, orderId, reason, 1);
    }
}
