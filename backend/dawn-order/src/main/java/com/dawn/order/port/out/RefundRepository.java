package com.dawn.order.port.out;

import com.dawn.order.entity.Refund;
import com.dawn.order.entity.enums.RefundStatus;

import java.util.List;
import java.util.Optional;

public interface RefundRepository {
    Refund save(Refund refund);
    Optional<Refund> findById(Long id);
    List<Refund> findBySubOrderId(Long subOrderId);
    Optional<Refund> findByReturnRequestId(Long returnRequestId);
    List<Refund> findByStatus(RefundStatus status);
}
