package com.dawn.order.dto;

import com.dawn.order.entity.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public record OrderDto(
        Long id,
        Long userId,
        Map<String, Object> shippingAddress,
        BigDecimal totalAmount,
        BigDecimal taxAmount,
        PaymentStatus paymentStatus,
        Instant reserveUntil,
        boolean invoiceRequested,
        List<SubOrderDto> subOrders,
        Instant createdAt) {
}
