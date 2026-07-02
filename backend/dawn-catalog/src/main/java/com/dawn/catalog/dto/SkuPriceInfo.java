package com.dawn.catalog.dto;

import java.math.BigDecimal;

public record SkuPriceInfo(
        Long skuId,
        String productName,
        String primaryImageUrl,
        BigDecimal unitPrice) {
}
