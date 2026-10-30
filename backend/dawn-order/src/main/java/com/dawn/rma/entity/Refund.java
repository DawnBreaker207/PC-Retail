package com.dawn.rma.entity;

import com.dawn.common.entity.AbstractMappedEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "refund")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class Refund extends AbstractMappedEntity {

    @Column(name = "return_request_id")
    private Long returnRequestId;

    @Column(name = "sub_order_id", nullable = false)
    private Long subOrderId;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private RefundStatus status = RefundStatus.PENDING;

    @Column(name = "refund_method", nullable = false, length = 20)
    private String refundMethod;

    @Column(name = "processed_at")
    private Instant processedAt;
}
