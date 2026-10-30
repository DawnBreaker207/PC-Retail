package com.dawn.common.event;

import java.util.List;

public record InventoryFailedEvent(Long orderId, String reason, List<Long> failedSkuIds) {}
