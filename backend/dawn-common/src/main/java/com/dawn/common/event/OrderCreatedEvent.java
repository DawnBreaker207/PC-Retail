package com.dawn.common.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderCreatedEvent(Long orderId, Long userId, int version) {
    public OrderCreatedEvent(Long orderId, Long userId) {
        this(orderId, userId, 1);
    }
}
