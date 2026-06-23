package com.dawn.order.dto;

import com.dawn.order.entity.enums.FulfillmentType;
import com.dawn.order.entity.enums.PaymentStatus;
import com.dawn.order.entity.enums.SubOrderStatus;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public record SubOrderDto(
        Long id,
        Long orderId,
        FulfillmentType fulfillmentType,
        BigDecimal subtotal,
        BigDecimal shippingFee,
        BigDecimal discountAmount,
        BigDecimal finalAmount,
        SubOrderStatus status,
        String trackingCode,
        List<Map<String, Object>> statusHistory,
        List<OrderItemDto> items,
        Instant createdAt) {
}
