package com.dawn.promotion.entity;

import com.dawn.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "flash_sale_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Hidden
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FlashSaleItem extends BaseEntity {
    @Column(name = "campaign_id", nullable = false, unique = true)
    private Long campaignId;

    @Column(name = "sku_id", nullable = false, unique = true)
    private String skuId;

    @Column(name = "flash_price", nullable = false)
    private BigDecimal flashPrice;

    @Column(name = "quantity_limit", nullable = false)
    private int quantityLimit;
}
