package com.dawn.inventory.controller.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record StocktakeRequest(
        Long warehouseId,
        @NotEmpty List<Item> items
) {
    public record Item(
            @NotNull Long skuId,
            int countedQuantity
    ) {}
}
