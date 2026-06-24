package com.dawn.cart.infra.persistence;

import com.dawn.cart.entity.CartItem;
import com.dawn.cart.port.out.CartItemRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaCartItemRepository extends JpaRepository<CartItem, Long>, CartItemRepository {

    @Override
    default CartItem save(CartItem item) {
        return this.saveAndFlush(item);
    }

    @Override
    List<CartItem> findByUserId(Long userId);

    @Override
    Optional<CartItem> findByUserIdAndSkuId(Long userId, Long skuId);

    @Override
    void deleteByUserIdAndSkuId(Long userId, Long skuId);

    @Override
    void deleteByUserId(Long userId);

    @Override
    int countByUserId(Long userId);

    int updateQuantityWithVersion(Long userId, Long skuId, int quantity, long expectedVersion);
}
