package com.dawn.cart.port.out;

import com.dawn.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


public interface CartItemRepository {
    Optional<CartItem> findByUserIdAndSkuId(Long userId, Long skuId);

    List<CartItem> findByUserId(Long userId);

    int countByUserId(Long userId);

    CartItem save(CartItem item);

    void deleteByUserIdAndSkuId(Long userId, Long skuId);

    void deleteByUserId(Long userId);


}
