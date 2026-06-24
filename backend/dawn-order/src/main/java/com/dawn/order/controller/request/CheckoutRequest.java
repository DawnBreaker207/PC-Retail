package com.dawn.order.controller.request;

import com.dawn.order.entity.enums.FulfillmentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record CheckoutRequest(
        @NotBlank String idempotencyKey,
        @NotNull FulfillmentType fulfillmentType,
        Map<String, Object> shippingAddress,
        String voucherCode,
        Map<String, Object> taxInfo
) {
}
