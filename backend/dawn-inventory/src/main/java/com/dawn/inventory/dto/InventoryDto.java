package com.dawn.inventory.dto;

public record InventoryDto(
        Long skuId,
        String skuName,
        int stockQuantity,
        int reservedQuantity,
        int availableQuantity) {
}
