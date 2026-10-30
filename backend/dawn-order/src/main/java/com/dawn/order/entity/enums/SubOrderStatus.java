package com.dawn.order.entity.enums;

public enum SubOrderStatus {
    PENDING_PAYMENT,
    AWAITING_INVENTORY,
    PAID,
    PROCESSING,
    SHIPPED,
    READY_FOR_PICKUP,
    DELIVERED,
    RETURN_REQUESTED,
    RETURNED,
    EXPIRED,
    CANCELLING,
    CANCELLED,
    CANCELLED_OUT_OF_STOCK
}
