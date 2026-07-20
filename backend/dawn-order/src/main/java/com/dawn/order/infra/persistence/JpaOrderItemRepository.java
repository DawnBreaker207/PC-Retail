package com.dawn.order.infra.persistence;

import com.dawn.order.entity.OrderItem;
import com.dawn.order.port.out.OrderItemRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaOrderItemRepository extends JpaRepository<OrderItem, Long>, OrderItemRepository {

    @Override
    List<OrderItem> saveAll(List<OrderItem> items);

    @Override
    boolean existsBySkuId(Long skuId);

    @Override
    List<OrderItem> findBySubOrderId(Long subOrderId);
}
