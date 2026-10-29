package com.dawn.common.event;

import java.util.List;

public record InventoryReleasedEvent(Long orderId, List<InventoryLine> items) {
}
