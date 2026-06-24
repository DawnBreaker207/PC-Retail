package com.dawn.order.dto;

import com.dawn.order.entity.enums.FulfillmentType;
import com.dawn.order.entity.enums.SubOrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public record SubOrderDto(
        Long id,
        FulfillmentType fulfillmentType,
        BigDecimal subtotal,
        BigDecimal shippingFee,
        BigDecimal discountAmount,
        BigDecimal finalAmount,
        SubOrderStatus status,
        List<OrderItemDto> items) {
}
