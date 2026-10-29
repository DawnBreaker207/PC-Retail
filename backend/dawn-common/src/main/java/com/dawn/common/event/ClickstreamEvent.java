package com.dawn.common.event;

import java.math.BigDecimal;
import java.util.Map;

/** Hành vi xem linh kiện của khách; dawn-ai dùng để tính vector sở thích. */
public record ClickstreamEvent(
        Long userId,
        Long skuId,
        String componentCategory,
        BigDecimal price,
        Map<String, Object> specs) {
}
