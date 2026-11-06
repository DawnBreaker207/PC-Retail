package com.dawn.order.entity;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "warranty_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Hidden
public class WarrantyItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_item_id", nullable = false)
    private Long orderItemId;

    @Column(name = "sub_order_id", nullable = false)
    private Long subOrderId;

    @Column(name = "sku_id", nullable = false)
    private Long skuId;

    @Column(name = "shop_months", nullable = false)
    @Builder.Default
    private int shopMonths = 12;

    @Column(name = "mfr_months", nullable = false)
    @Builder.Default
    private int mfrMonths = 0;

    @Column(name = "activated_at")
    private Instant activatedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "serial_number", length = 100)
    private String serialNumber;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
