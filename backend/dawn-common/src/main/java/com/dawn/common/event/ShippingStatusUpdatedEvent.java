package com.dawn.common.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ShippingStatusUpdatedEvent(Long orderId, String trackingCode, String status) {
}
