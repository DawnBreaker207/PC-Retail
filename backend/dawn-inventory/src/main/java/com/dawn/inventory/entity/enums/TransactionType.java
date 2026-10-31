package com.dawn.inventory.entity.enums;

public enum TransactionType {
    RESERVE,
    RELEASE,
    DEDUCT,
    RESTOCK,
    ADJUST,
    // docs §13.6: split ADJUST and add RETURN
    ADJUSTMENT_IN,
    ADJUSTMENT_OUT,
    RETURN
}
