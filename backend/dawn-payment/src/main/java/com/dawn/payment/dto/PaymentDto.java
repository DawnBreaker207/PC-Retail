package com.dawn.payment.dto;

import com.dawn.payment.entity.enums.PaymentMethod;
import com.dawn.payment.entity.enums.PaymentResultStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentDto(
        Long id,
        Long orderId,
        PaymentMethod method,
        BigDecimal amount,
        PaymentResultStatus status,
        String transactionRef,
        Instant createdAt) {
}
