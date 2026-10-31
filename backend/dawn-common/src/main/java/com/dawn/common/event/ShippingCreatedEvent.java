package com.dawn.common.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ShippingCreatedEvent(Long orderId, Long shippingId, BigDecimal shippingFee, Instant estimatedDelivery) {
}
