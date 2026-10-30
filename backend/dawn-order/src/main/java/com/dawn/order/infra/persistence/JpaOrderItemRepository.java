package com.dawn.order.infra.persistence;

import com.dawn.order.entity.OrderItem;
import com.dawn.order.port.out.OrderItemRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaOrderItemRepository extends JpaRepository<OrderItem, Long>, OrderItemRepository {

    @Override
    boolean existsBySkuId(Long skuId);

    @Override
    List<OrderItem> findBySubOrderId(Long subOrderId);

    @Query("""
            SELECT COUNT(oi) > 0 FROM OrderItem oi
            JOIN SubOrder so ON so.id = oi.subOrderId
            JOIN Order o ON o.id = so.orderId
            WHERE oi.skuId = :skuId
              AND o.userId = :userId
              AND so.status = 'DELIVERED'
            """)
    boolean existsBySkuIdAndUserIdAndDelivered(@Param("skuId") Long skuId, @Param("userId") Long userId);
}
