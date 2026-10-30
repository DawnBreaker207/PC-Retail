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

    /**
     * Ghi số lượng tuyệt đối cho dòng (user, sku), 1 câu lệnh duy nhất.
     * Diệt race check-then-act của 2 tx cùng tạo dòng (unique violation).
     */
    void upsertMemberQuantity(Long userId, Long skuId, int quantity);

    void deleteByUserIdAndSkuId(Long userId, Long skuId);

    void deleteByUserId(Long userId);

    Optional<CartItem> findByGuestTokenAndSkuId(String guestToken, Long skuId);

    List<CartItem> findByGuestToken(String guestToken);

    void deleteByGuestTokenAndSkuId(String guestToken, Long skuId);

    void deleteByGuestToken(String guestToken);

}
