package com.dawn.order.port.out;

import com.dawn.order.entity.SubOrder;
import com.dawn.order.entity.enums.SubOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface SubOrderRepository {
    SubOrder save(SubOrder subOrder);

    default void saveAll(List<SubOrder> subOrders) {
        subOrders.forEach(this::save);
    }

    Optional<SubOrder> findById(Long id);

    List<SubOrder> findByOrderId(Long orderId);

    Page<SubOrder> findByStatus(SubOrderStatus status, Pageable pageable);

    List<SubOrder> findByStatusAndCreatedAtBefore(SubOrderStatus status, Instant threshold);

    List<SubOrder> findByStatusAndReservedUntilBefore(SubOrderStatus status, Instant threshold);

    boolean existsByIdAndStatus(Long id, SubOrderStatus status);

    boolean isDelivered(Long subOrderId);

}
