package com.dawn.order.entity;

import com.dawn.common.entity.AbstractMappedEntity;
import com.dawn.order.entity.enums.PaymentStatus;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Hidden
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Order extends AbstractMappedEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "shipping_address", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> shippingAddress;

    @Column(name = "total_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "tax_amount", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "reserved_until")
    private Instant reservedUntil;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 100)
    private String idempotencyKey;

    @Column(name = "created_by_type", nullable = false, length = 20)
    @Builder.Default
    private String createdByType = "SELF";

    @Column(name = "assisted_by_staff_id")
    private Long assistedByStaffId;

    @Column(name = "invoice_requested", nullable = false)
    @Builder.Default
    private boolean invoiceRequested = false;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tax_info", columnDefinition = "jsonb")
    private Map<String, Object> taxInfo;
}
