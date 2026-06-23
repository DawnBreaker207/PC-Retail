package com.dawn.common.event;

public record OrderCreatedEvent(Long orderId, Long userId) {
}
