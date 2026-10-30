package com.dawn.common.event;

import tools.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record InventoryDeductedEvent(Long orderId, List<InventoryLine> items, int version) {
    public InventoryDeductedEvent(Long orderId, List<InventoryLine> items) {
        this(orderId, items, 1);
    }
}
