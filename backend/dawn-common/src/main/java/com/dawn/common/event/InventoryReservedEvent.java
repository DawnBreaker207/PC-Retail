package com.dawn.common.event;

import java.util.List;

public record InventoryReservedEvent(Long orderId, List<InventoryLine> items) {
}
