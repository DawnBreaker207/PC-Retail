package com.dawn.common.event;

import tools.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ShippingStatusUpdatedEvent(Long orderId, String trackingCode, String status) {
}
