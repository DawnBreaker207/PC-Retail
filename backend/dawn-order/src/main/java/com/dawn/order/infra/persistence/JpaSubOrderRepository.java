package com.dawn.order.infra.persistence;

import com.dawn.order.entity.SubOrder;
import com.dawn.order.entity.enums.SubOrderStatus;
import com.dawn.order.port.out.SubOrderRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface JpaSubOrderRepository extends JpaRepository<SubOrder, Long>, SubOrderRepository {
    @Override
    List<SubOrder> findByOrderId(Long orderId);

    @Override
    List<SubOrder> findByStatusAndCreatedAtBefore(SubOrderStatus status, Instant threshold);

    @Override
    @Query("""
            SELECT s
            FROM SubOrder s
            JOIN Order o ON o.id = s.orderId
            WHERE s.status = :status
            AND o.reservedUntil < :now
            """)
    List<SubOrder> findByStatusAndReservedUntilBefore(
            @Param("status") SubOrderStatus status,
            @Param("now") Instant threshold);

    @Override
    boolean existsByIdAndStatus(Long id, SubOrderStatus status);

    @Override
    default boolean isDelivered(Long subOrderId) {
        return existsByIdAndStatus(subOrderId, SubOrderStatus.DELIVERED);
    }

    @Override
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE SubOrder s SET s.status = :target WHERE s.id = :id AND s.status = :expected")
    int transitionIfStatus(
            @Param("id") Long id,
            @Param("expected") SubOrderStatus expected,
            @Param("target") SubOrderStatus target);
}
