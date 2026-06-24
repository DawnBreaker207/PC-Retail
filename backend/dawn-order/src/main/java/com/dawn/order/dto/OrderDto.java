package com.dawn.order.dto;

import com.dawn.order.entity.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public record OrderDto(
        Long id,
        Long userId,
        BigDecimal totalAmount,
        PaymentStatus paymentStatus,
        Instant reserveUntil,
        List<SubOrderDto> subOrders) {
}
