package com.dawn.wishlist.service;

import com.dawn.common.exception.wrapper.ConflictException;
import com.dawn.wishlist.dto.WishlistItemDto;
import com.dawn.wishlist.entity.WishlistItem;
import com.dawn.wishlist.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WishlistService {

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

    @Transactional(readOnly = true)
    public List<WishlistItemDto> getWishlist(Long userId) {
        return wishlistRepository.findByUserIdOrderByAddedAtDesc(userId)
                .stream()
                .map(WishlistItemDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean isWishlisted(Long userId, Long skuId) {
        return wishlistRepository.existsByUserIdAndSkuId(userId, skuId);
    }

    @Transactional(readOnly = true)
    public List<Long> getUsersWithSku(Long skuId) {
        return wishlistRepository.findBySkuId(skuId)
                .stream()
                .map(WishlistItem::getUserId)
                .distinct()
                .toList();
    }
}
