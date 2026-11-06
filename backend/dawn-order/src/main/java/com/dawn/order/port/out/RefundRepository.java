package com.dawn.order.port.out;

import com.dawn.order.entity.Refund;

import java.util.List;
import java.util.Optional;

public interface RefundRepository {
    Refund save(Refund refund);
    Optional<Refund> findById(Long id);
    List<Refund> findBySubOrderId(Long subOrderId);
}
