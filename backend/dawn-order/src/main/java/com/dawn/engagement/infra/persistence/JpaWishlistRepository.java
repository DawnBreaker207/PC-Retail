package com.dawn.engagement.infra.persistence;

import com.dawn.engagement.entity.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaWishlistRepository extends JpaRepository<WishlistItem, Long> {
    List<WishlistItem> findByUserId(Long userId);

    Optional<WishlistItem> findByUserIdAndSkuId(Long userId, Long skuId);
}
