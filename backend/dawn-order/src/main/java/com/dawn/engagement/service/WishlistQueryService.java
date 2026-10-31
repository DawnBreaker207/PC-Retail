package com.dawn.engagement.service;

import com.dawn.engagement.entity.WishlistItem;
import com.dawn.engagement.infra.persistence.JpaWishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WishlistQueryService {

    private final JpaWishlistRepository wishlistRepo;

    @Transactional(readOnly = true)
    public List<WishlistItem> listByUser(Long userId) {
        return wishlistRepo.findByUserId(userId);
    }
}
