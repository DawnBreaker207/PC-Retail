package com.dawn.common.contract;

import java.math.BigDecimal;

public record SkuPriceInfo(
        Long skuId,
        String productName,
        String primaryImageUrl,
        BigDecimal unitPrice) {
}
