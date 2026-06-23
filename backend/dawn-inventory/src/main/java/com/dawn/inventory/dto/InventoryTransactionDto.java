package com.dawn.inventory.dto;

import com.dawn.inventory.entity.enums.ReferenceType;
import com.dawn.inventory.entity.enums.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

public record InventoryTransactionDto(
        Long id,
        Long skuId,
        TransactionType type,
        int quantity,
        BigDecimal unitCost,
        ReferenceType referenceType,
        Long referenceId,
        Long createdBy,
        Instant createdAt) {
}
