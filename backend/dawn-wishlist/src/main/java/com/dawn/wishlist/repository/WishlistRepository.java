package com.dawn.wishlist.repository;

import com.dawn.wishlist.entity.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface WishlistRepository extends JpaRepository<WishlistItem, Long> {
    List<WishlistItem> findByUserIdOrderByAddedAtDesc(Long userId);
    boolean existsByUserIdAndSkuId(Long userId, Long skuId);
    @Modifying
    @Transactional
    void deleteByUserIdAndSkuId(Long userId, Long skuId);
    List<WishlistItem> findBySkuId(Long skuId);
}
