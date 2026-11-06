package com.dawn.order.infra.persistence;

import com.dawn.order.entity.Refund;
import com.dawn.order.port.out.RefundRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaRefundRepository extends JpaRepository<Refund, Long>, RefundRepository {

    @Override
    List<Refund> findBySubOrderId(Long subOrderId);
}
