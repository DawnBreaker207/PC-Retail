package com.dawn.inventory.dto;

public record StocktakeVarianceDto(
        Long skuId,
        int systemQuantity,
        int countedQuantity,
        int variance
) {}
