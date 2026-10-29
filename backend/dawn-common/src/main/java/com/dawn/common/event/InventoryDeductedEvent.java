package com.dawn.common.event;

import java.util.List;

public record InventoryDeductedEvent(Long orderId, List<InventoryLine> items) {
}
