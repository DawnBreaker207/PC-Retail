package com.dawn.common.event;

import tools.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ClickstreamEvent(
        Long userId,
        Long skuId,
        String componentCategory,
        BigDecimal price,
        Map<String, Object> specs,
        int version) {

    public ClickstreamEvent(Long userId, Long skuId, String componentCategory,
                             BigDecimal price, Map<String, Object> specs) {
        this(userId, skuId, componentCategory, price, specs, 1);
    }
}
