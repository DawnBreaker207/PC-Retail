package com.dawn.order.dto;

import com.dawn.order.entity.enums.FulfillmentType;
import com.dawn.order.entity.enums.ShippingMethod;
import com.dawn.order.entity.enums.SubOrderStatus;

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
        ShippingMethod shippingMethod,
        Long shipperId,
        Instant deliveryAttemptedAt,
        Instant codCollectedAt,
        List<OrderItemDto> items) {
}
