package com.dawn.cart.infra.persistence;

import com.dawn.cart.entity.CartItem;
import com.dawn.cart.port.out.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CartItemPersistenceAdapter implements CartItemRepository {
    private final JpaCartItemRepository jpaRepository;

    @Override
    public Optional<CartItem> findByUserIdAndSkuId(Long userId, Long skuId) {
        return jpaRepository.findByUserIdAndSkuId(userId, skuId);
    }

    @Override
    public List<CartItem> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId);
    }

    @Override
    public int countByUserId(Long userId) {
        return jpaRepository.countByUserId(userId);
    }

    @Override
    public CartItem save(CartItem item) {
        return jpaRepository.save(item);
    }

    @Override
    public void deleteByUserIdAndSkuId(Long userId, Long skuId) {
        jpaRepository.deleteByUserIdAndSkuId(userId, skuId);
    }

    @Override
    public void deleteByUserId(Long userId) {
        jpaRepository.deleteByUserId(userId);
    }
}
