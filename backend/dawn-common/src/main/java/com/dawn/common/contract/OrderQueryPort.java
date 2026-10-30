package com.dawn.common.contract;

public interface OrderQueryPort {
    boolean existsOrderItemBySkuId(Long skuId);
    boolean isSubOrderDelivered(Long subOrderId);
    boolean hasUserPurchasedSku(Long userId, Long skuId);
}
