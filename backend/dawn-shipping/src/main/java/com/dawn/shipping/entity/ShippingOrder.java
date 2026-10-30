package com.dawn.shipping.entity;

import com.dawn.shipping.entity.enums.Carrier;
import com.dawn.shipping.entity.enums.ShippingStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "shipping_orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "tracking_code", length = 100)
    private String trackingCode;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    @Builder.Default
    private Carrier carrier = Carrier.MOCK;

    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    @Builder.Default
    private ShippingStatus status = ShippingStatus.PENDING;

    @Column(name = "from_address", columnDefinition = "TEXT")
    private String fromAddress;

    @Column(name = "to_address", nullable = false, columnDefinition = "TEXT")
    private String toAddress;

    @Column(name = "weight_grams")
    private Integer weightGrams;

    @Column(name = "shipping_fee", precision = 15, scale = 2)
    private BigDecimal shippingFee;

    @Column(name = "estimated_delivery")
    private Instant estimatedDelivery;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
