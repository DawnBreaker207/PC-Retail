package com.dawn.wishlist.service;

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
public class WishlistQueryService {

    private final WishlistRepository wishlistRepository;

    @Transactional(readOnly = true)
    public List<WishlistItemDto> listByUser(Long userId) {
        return wishlistRepository.findByUserIdOrderByAddedAtDesc(userId)
                .stream()
                .map(WishlistItemDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean existsInWishlist(Long userId, Long skuId) {
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
