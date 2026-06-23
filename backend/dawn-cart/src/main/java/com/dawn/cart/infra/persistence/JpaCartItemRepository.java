package com.dawn.cart.infra.persistence;

import com.dawn.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaCartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUserId(Long userId);

    Optional<CartItem> findByUserIdAndSkuId(Long userId, Long skuId);

    void deleteByUserIdAndSkuId(Long userId, Long skuId);

    void deleteByUserId(Long userId);

    int countByUserId(Long userId);

    int updateQuantityWithVersion(Long userId, Long skuId, int quantity, long expectedVersion);
}
