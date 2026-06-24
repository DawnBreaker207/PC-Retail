package com.dawn.order.infra.persistence;

import com.dawn.order.entity.Order;
import com.dawn.order.port.out.OrderRepositoryPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaOrderRepository extends JpaRepository<Order, Long>, OrderRepositoryPort {
    @Override
    Optional<Order> findByIdempotencyKey(String idempotencyKey);

    @Override
    Page<Order> findByUserId(Long userId, Pageable pageable);
}
