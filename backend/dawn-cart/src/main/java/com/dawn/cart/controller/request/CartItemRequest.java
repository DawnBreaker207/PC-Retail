package com.dawn.cart.controller.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CartItemRequest(
        @NotNull Long skuId,
        @NotNull @Min(1) Integer quantity) {
}
