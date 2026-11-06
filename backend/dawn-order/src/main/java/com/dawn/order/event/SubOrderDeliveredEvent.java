package com.dawn.order.event;

import com.dawn.order.entity.OrderItem;

import java.util.List;

public record SubOrderDeliveredEvent(Long subOrderId, Long orderId, List<OrderItem> items) {
}
