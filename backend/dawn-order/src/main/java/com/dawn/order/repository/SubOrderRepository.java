package com.dawn.order.repository;

import com.dawn.order.entity.SubOrder;
import com.dawn.order.entity.enums.SubOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubOrderRepository extends JpaRepository<SubOrder, Long> {
    List<SubOrder> findByOrderId(Long orderId);

    Page<SubOrder> FindByStatus(SubOrderStatus status, Pageable pageable);
}
