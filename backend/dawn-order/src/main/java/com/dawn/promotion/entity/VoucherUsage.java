package com.dawn.promotion.entity;

import com.dawn.common.entity.AbstractMappedEntity;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "voucher_usage",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_voucher_usage_voucher_user",
                columnNames = {"voucher_id", "user_id"}
        )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Hidden
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class VoucherUsage extends AbstractMappedEntity {
    @Column(name = "voucher_id", nullable = false)
    private Long voucherId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "discount_amount", nullable = false)
    private BigDecimal discountAmount;

}
