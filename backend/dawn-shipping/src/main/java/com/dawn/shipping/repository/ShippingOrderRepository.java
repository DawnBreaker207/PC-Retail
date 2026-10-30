package com.dawn.shipping.repository;

import com.dawn.shipping.entity.ShippingOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShippingOrderRepository extends JpaRepository<ShippingOrder, Long> {
    Optional<ShippingOrder> findByOrderId(Long orderId);
    List<ShippingOrder> findByStatus(String status);
    Page<ShippingOrder> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Optional<ShippingOrder> findByTrackingCode(String trackingCode);
}
