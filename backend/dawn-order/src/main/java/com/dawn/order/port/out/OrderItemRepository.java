package com.dawn.order.port.out;

import com.dawn.order.entity.OrderItem;

import java.util.List;

public interface OrderItemRepository {
    List<OrderItem> saveAll(List<OrderItem> items);

    List<OrderItem> findBySubOrderId(Long subOrderId);

    List<OrderItem> findBySubOrderIdIn(List<Long> subOrderId);

    boolean existsBySkuId(Long skuId);
}
