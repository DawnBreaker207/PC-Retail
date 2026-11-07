package com.dawn.inventory.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "stocktake_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StocktakeItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stocktake_id", nullable = false)
    private Long stocktakeId;

    @Column(name = "sku_id", nullable = false)
    private Long skuId;

    @Column(name = "system_quantity", nullable = false)
    private int systemQuantity;

    @Column(name = "counted_quantity", nullable = false)
    private int countedQuantity;

    public int getVariance() {
        return countedQuantity - systemQuantity;
    }
}
