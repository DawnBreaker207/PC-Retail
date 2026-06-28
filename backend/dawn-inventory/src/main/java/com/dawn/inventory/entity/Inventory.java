package com.dawn.inventory.entity;

import com.dawn.common.entity.AuditableEntity;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Entity
@Table(name = "inventory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Hidden
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Inventory extends AuditableEntity {

    @Column(name = "sku_id", nullable = false, unique = true)
    private Long skuId;

    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity;

    @Column(name = "reserved_quantity", nullable = false)
    private int reservedQuantity;

    @PrePersist
    @PreUpdate
    void touch() {
        this.setUpdatedAt(Instant.now());
    }

    public int getAvailableQuantity() {
        return stockQuantity - reservedQuantity;
    }
}
