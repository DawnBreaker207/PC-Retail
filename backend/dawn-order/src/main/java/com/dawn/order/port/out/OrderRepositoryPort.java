package com.dawn.order.port.out;

import com.dawn.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface OrderRepositoryPort {
    Order save(Order order);

    Optional<Order> findById(Long id);

    Optional<Order> findByIdempotencyKey(String idempotencyKey);

    Page<Order> findByUserId(Long userId, Pageable pageable);
}
