package com.dawn.reporting.entity;

import com.dawn.common.entity.AbstractMappedEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "stock_take_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class StockTakeItem extends AbstractMappedEntity {

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Column(name = "sku_id", nullable = false)
    private Long skuId;

    @Column(name = "system_quantity", nullable = false)
    private int systemQuantity;

    @Column(name = "counted_quantity", nullable = false)
    private int countedQuantity;

    @Transient
    public int getVariance() {
        return countedQuantity - systemQuantity;
    }
}
