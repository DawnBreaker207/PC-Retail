package com.dawn.order.entity.enums;

public enum PaymentStatus {
    PENDING,
    COD_PENDING,  // COD order: payment collected at delivery
    PAID,
    FAILED,
    EXPIRED
}
