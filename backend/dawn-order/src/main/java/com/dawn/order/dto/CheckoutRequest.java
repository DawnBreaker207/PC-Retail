package com.dawn.order.dto;

import com.dawn.order.entity.enums.FulfillmentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record CheckoutRequest(
        @NotBlank String idempotencyKey,
        @NotNull FulfillmentType fulfillmentType,
        String shippingAddressId,
        String voucherCode,
        boolean invoiceRequested,
        Map<String, Object> taxInfo) {
}
