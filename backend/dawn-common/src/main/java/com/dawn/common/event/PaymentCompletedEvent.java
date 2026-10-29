package com.dawn.common.event;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentCompletedEvent(
        Long paymentId,
        Long orderId,
        BigDecimal amount,
        String transactionRef,
        Instant settledAt) {
}
