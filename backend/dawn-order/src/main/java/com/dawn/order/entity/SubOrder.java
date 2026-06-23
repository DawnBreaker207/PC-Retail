package com.dawn.order.entity;

import com.dawn.common.entity.AuditableEntity;
import com.dawn.order.entity.enums.FulfillmentType;
import com.dawn.order.entity.enums.SubOrderStatus;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "sub_order")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Hidden
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SubOrder extends AuditableEntity {

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "fulfillment_type", nullable = false, length = 10)
    private FulfillmentType fulfillmentType;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "shipping_fee", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal shippingFee = BigDecimal.ZERO;

    @Column(name = "discount_amount", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "final_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal finalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private SubOrderStatus status = SubOrderStatus.PENDING_PAYMENT;

    @Column(name = "tracking_code", length = 100)
    private String trackingCode;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> carrier;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "webhook_log", columnDefinition = "jsonb")
    private Map<String, Object> webhookLog;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "status_history", nullable = false, columnDefinition = "jsonb")
    @Builder.Default
    private List<Map<String, Object>> statusHistory = new ArrayList<>();
}
