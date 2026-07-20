package com.dawn.order.dto;

public interface OrderQueryPort {
    boolean existsOrderItemBySkuId(Long skuId);
    boolean isSubOrderDelivered(Long subOrderId);
}
