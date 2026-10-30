package com.dawn.engagement.entity;

import com.dawn.common.entity.AbstractMappedEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "wishlist_item", uniqueConstraints = {
        @UniqueConstraint(name = "uq_wishlist_user_sku", columnNames = {"user_id", "sku_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class WishlistItem extends AbstractMappedEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "sku_id", nullable = false)
    private Long skuId;
}
