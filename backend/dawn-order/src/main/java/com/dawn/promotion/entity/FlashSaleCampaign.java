package com.dawn.promotion.entity;

import com.dawn.common.entity.AbstractMappedEntity;
import com.dawn.promotion.entity.enums.FlashSaleStatus;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Entity
@Table(name = "flash_sale_campaign")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Hidden
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FlashSaleCampaign extends AbstractMappedEntity {

    @Column(name = "name")
    private String name;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time", nullable = false)
    private Instant endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private FlashSaleStatus status = FlashSaleStatus.UPCOMING;
}
