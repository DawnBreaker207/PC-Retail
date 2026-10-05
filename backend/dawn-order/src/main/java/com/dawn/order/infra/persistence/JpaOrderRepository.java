package com.dawn.order.infra.persistence;

import com.dawn.order.entity.Order;
import com.dawn.order.port.out.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaOrderRepository extends JpaRepository<Order, Long>, OrderRepository {
    @Override
    Optional<Order> findByIdempotencyKey(String idempotencyKey);

    @Override
    Optional<Order> findByUserIdAndIdempotencyKey(Long userId, String idempotencyKey);

    @Override
    Page<Order> findByUserId(Long userId, Pageable pageable);

    @Override
    Page<Order> findAll(Pageable pageable);
}
