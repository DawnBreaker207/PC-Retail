package com.dawn.engagement.service;

import com.dawn.engagement.entity.WishlistItem;
import com.dawn.engagement.infra.persistence.JpaWishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WishlistCommandService {

    private final JpaWishlistRepository wishlistRepo;

    @Transactional
    public WishlistItem addItem(Long userId, Long skuId) {
        return wishlistRepo.findByUserIdAndSkuId(userId, skuId)
                .orElseGet(() -> wishlistRepo.save(WishlistItem.builder().userId(userId).skuId(skuId).build()));
    }

    @Transactional
    public void removeItem(Long userId, Long skuId) {
        wishlistRepo.findByUserIdAndSkuId(userId, skuId)
                .ifPresent(wishlistRepo::delete);
    }
}
