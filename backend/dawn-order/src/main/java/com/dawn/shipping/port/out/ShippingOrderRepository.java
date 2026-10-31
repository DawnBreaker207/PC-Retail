package com.dawn.shipping.port.out;

import com.dawn.shipping.entity.ShippingOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ShippingOrderRepository {

    ShippingOrder save(ShippingOrder order);

    Optional<ShippingOrder> findByTrackingCode(String trackingCode);

    Optional<ShippingOrder> findByOrderId(Long orderId);

    Page<ShippingOrder> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
