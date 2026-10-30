package com.dawn.promotion.port.out;

public interface FlashSaleRepository {
    boolean isSkuInActiveFlashSale(Long skuId);
}
