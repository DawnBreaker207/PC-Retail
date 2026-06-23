package com.dawn.cart.repository;

import com.dawn.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUserId(Long userId);

    Optional<CartItem> findByUserIdAndSkuId(Long userId, Long skuId);

    void deleteByUserIdAndSkuId(Long userId, Long skuId);

    void deleteByUserId(Long userId);

    int countByUserId(Long userId);
}
