package com.dawn.payment.entity;

import com.dawn.common.entity.AbstractMappedEntity;
import com.dawn.payment.entity.enums.PaymentMethod;
import com.dawn.payment.entity.enums.PaymentResultStatus;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Hidden
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Payment extends AbstractMappedEntity {

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false, length = 20)
    private PaymentMethod method;

    @Column(name = "installment_months")
    private Integer installmentMonths;

    @Column(name = "amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private PaymentResultStatus status = PaymentResultStatus.PENDING;

    @Column(name = "transaction_ref")
    private String transactionRef;

    @Column(name = "settled_at")
    private Instant settledAt;

    @Column(name = "settlement_batch_id", nullable = true, updatable = false)
    private String settlementBatchId;
}
