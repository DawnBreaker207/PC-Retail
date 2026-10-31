package com.dawn.common.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PaymentCompletedEvent(
        Long paymentId,
        Long orderId,
        BigDecimal amount,
        String transactionRef,
        Instant settledAt,
        int version) {

    public PaymentCompletedEvent(Long paymentId, Long orderId, BigDecimal amount,
                                  String transactionRef, Instant settledAt) {
        this(paymentId, orderId, amount, transactionRef, settledAt, 1);
    }
}
