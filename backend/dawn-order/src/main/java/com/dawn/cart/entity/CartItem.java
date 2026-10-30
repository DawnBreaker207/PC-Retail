package com.dawn.cart.entity;

import com.dawn.common.entity.AbstractMappedEntity;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Entity
@Table(
        name = "cart_item",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "sku_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Hidden
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CartItem extends AbstractMappedEntity {

    @Column(name = "user_id")
    private Long userId;

    // Giỏ khách vãng lai: user_id null + guest_token (CHECK 1-trong-2 ở DB)
    @Column(name = "guest_token", length = 100)
    private String guestToken;

    @Column(name = "sku_id", nullable = false)
    private Long skuId;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "added_at", nullable = false)
    @Builder.Default
    private Instant addedAt = Instant.now();

    @Version
    @Column(nullable = false)
    @Builder.Default
    private Long version = 0L;
}
