package com.dawn.order.infra.persistence;

import com.dawn.order.entity.SubOrder;
import com.dawn.order.entity.enums.SubOrderStatus;
import com.dawn.order.port.out.SubOrderRepositoryPort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface JpaSubOrderRepository extends JpaRepository<SubOrder, Long>, SubOrderRepositoryPort {
    @Override
    List<SubOrder> findByOrderId(Long orderId);

    @Override
    List<SubOrder> findByStatusAndCreatedAtBefore(SubOrderStatus status, Instant threshold);
}
