package com.dawn.order.port.out;

import com.dawn.order.entity.OrderItem;

import java.util.List;

public interface OrderItemRepositoryPort {
    List<OrderItem> saveAll(List<OrderItem> items);

    List<OrderItem> findBySubOrderId(Long subOrderId);

    boolean existsBySkuId(Long skuId);
}
