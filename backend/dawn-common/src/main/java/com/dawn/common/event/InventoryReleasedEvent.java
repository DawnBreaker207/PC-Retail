package com.dawn.common.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record InventoryReleasedEvent(Long orderId, List<InventoryLine> items, int version) {
    public InventoryReleasedEvent(Long orderId, List<InventoryLine> items) {
        this(orderId, items, 1);
    }
}
