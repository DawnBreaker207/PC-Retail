package com.dawn.inventory.entity.enums;

public enum TransactionType {
    RESERVE,
    RELEASE,
    DEDUCT,
    RESTOCK,
    ADJUST,
    // docs §13.6: tách ADJUST + thêm RETURN
    ADJUSTMENT_IN,
    ADJUSTMENT_OUT,
    RETURN
}
