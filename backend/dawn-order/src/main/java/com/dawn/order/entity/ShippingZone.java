package com.dawn.order.entity;

import com.dawn.common.entity.AbstractMappedEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "shipping_zone")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class ShippingZone extends AbstractMappedEntity {

    @Column(name = "zone_code", nullable = false, unique = true, length = 50)
    private String zoneCode;

    // JSON match đơn giản: {"city": "HCM", "district": "..."} — null = match all
    @Column(name = "match_city", length = 100)
    private String matchCity;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal fee;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
