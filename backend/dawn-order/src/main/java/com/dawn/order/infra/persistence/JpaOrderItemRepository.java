package com.dawn.order.infra.persistence;

import com.dawn.order.entity.OrderItem;
import com.dawn.order.port.out.OrderItemRepositoryPort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaOrderItemRepository extends JpaRepository<OrderItem, Long>, OrderItemRepositoryPort {
    @Override
    List<OrderItem> findBySubOrderId(Long subOrderId);
}
