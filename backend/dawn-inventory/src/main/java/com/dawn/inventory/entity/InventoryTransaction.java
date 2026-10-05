package com.dawn.inventory.entity;

import com.dawn.common.entity.AbstractMappedEntity;
import com.dawn.inventory.entity.enums.ReferenceType;
import com.dawn.inventory.entity.enums.TransactionType;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "inventory_transaction")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Hidden
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class InventoryTransaction extends AbstractMappedEntity {
    @Column(name = "sku_id", nullable = false)
    private Long skuId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "unit_cost", precision = 14, scale = 2)
    private BigDecimal unitCost;

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", length = 30)
    private ReferenceType referenceType;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "created_by")
    private Long createdBy;

    // docs §13.6: reason cho ADJUSTMENT_* / RETURN
    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;
}
