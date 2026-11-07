package com.dawn.order.infra.persistence;

import com.dawn.order.entity.Refund;
import com.dawn.order.entity.enums.RefundStatus;
import com.dawn.order.port.out.RefundRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaRefundRepository extends JpaRepository<Refund, Long>, RefundRepository {

    @Override
    List<Refund> findBySubOrderId(Long subOrderId);

    @Override
    Optional<Refund> findByReturnRequestId(Long returnRequestId);

    List<Refund> findByStatus(RefundStatus status);
}
