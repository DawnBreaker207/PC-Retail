package com.dawn.inventory.controller.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RestockRequest(
        @NotNull @Min(1) Integer quantity,
        BigDecimal unitCost
) {
}
