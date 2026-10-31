package com.dawn.wishlist.service;

import com.dawn.common.exception.wrapper.ConflictException;
import com.dawn.wishlist.dto.WishlistItemDto;
import com.dawn.wishlist.entity.WishlistItem;
import com.dawn.wishlist.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WishlistCommandService {

    private final WishlistRepository wishlistRepository;

    @Transactional
    public WishlistItemDto addItem(Long userId, Long skuId) {
        if (wishlistRepository.existsByUserIdAndSkuId(userId, skuId)) {
            throw new ConflictException("SKU " + skuId + " already in wishlist");
        }
        WishlistItem item = WishlistItem.builder()
                .userId(userId)
                .skuId(skuId)
                .build();
        return WishlistItemDto.from(wishlistRepository.save(item));
    }

    @Transactional
    public void removeItem(Long userId, Long skuId) {
        wishlistRepository.deleteByUserIdAndSkuId(userId, skuId);
    }
}
