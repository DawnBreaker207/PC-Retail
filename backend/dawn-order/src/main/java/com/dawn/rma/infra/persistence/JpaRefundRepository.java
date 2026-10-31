package com.dawn.rma.infra.persistence;

import com.dawn.rma.entity.Refund;
import com.dawn.rma.port.out.RefundRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaRefundRepository extends JpaRepository<Refund, Long>, RefundRepository {
}
