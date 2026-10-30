package com.dawn.shipping.dto;

import java.util.List;

public record OrderCreatedEvent(
        Long orderId,
        Long userId,
        String toAddress,
        List<OrderItemLine> items,
        Integer totalWeightGrams
) {}
