package com.dawn.order.port.out;

import com.dawn.order.entity.OrderItem;

import java.util.List;

public interface OrderItemRepository {
    OrderItem save(OrderItem item);

    default List<OrderItem> saveAll(List<OrderItem> items) {
        return items.stream().map(this::save).toList();
    }

    List<OrderItem> findBySubOrderId(Long subOrderId);

    List<OrderItem> findBySubOrderIdIn(List<Long> subOrderId);

    boolean existsBySkuId(Long skuId);
}
