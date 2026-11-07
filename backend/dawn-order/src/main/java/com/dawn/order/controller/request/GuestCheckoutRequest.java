package com.dawn.order.controller.request;

import com.dawn.order.entity.enums.FulfillmentType;
import com.dawn.order.entity.enums.PaymentMethod;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record GuestCheckoutRequest(
        @NotBlank String idempotencyKey,
        @NotNull FulfillmentType fulfillmentType,
        Map<String, Object> shippingAddress,
        @NotBlank String guestToken,
        @Email String guestEmail,
        String guestPhone,
        PaymentMethod paymentMethod) {

    public PaymentMethod resolvedPaymentMethod() {
        return paymentMethod != null ? paymentMethod : PaymentMethod.ONLINE;
    }
}
