package com.dawn.order.entity.enums;

public enum SubOrderStatus {
    PENDING_PAYMENT,
    AWAITING_INVENTORY,
    PAID,
    PROCESSING,
    READY_TO_SHIP,       // packed, waiting for shipper pickup
    OUT_FOR_DELIVERY,    // shipper has picked up, en route
    DELIVERY_FAILED,     // delivery attempt failed (not home, wrong address)
    SHIPPED,             // carrier (GHN/etc) flow — kept for hybrid
    READY_FOR_PICKUP,
    DELIVERED,
    RETURN_REQUESTED,
    RETURNED,
    EXPIRED,
    CANCELLING,
    CANCELLED,
    CANCELLED_OUT_OF_STOCK
}
