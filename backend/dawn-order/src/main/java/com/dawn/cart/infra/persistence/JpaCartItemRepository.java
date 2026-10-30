package com.dawn.cart.infra.persistence;

import com.dawn.cart.entity.CartItem;
import com.dawn.cart.port.out.CartItemRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Override
    Optional<CartItem> findByGuestTokenAndSkuId(String guestToken, Long skuId);

    @Override
    List<CartItem> findByGuestToken(String guestToken);

    @Override
    void deleteByGuestTokenAndSkuId(String guestToken, Long skuId);

    @Override
    void deleteByGuestToken(String guestToken);

    @Override
    @Modifying
    @Query(value = """
            INSERT INTO cart_item (user_id, guest_token, sku_id, quantity, added_at, version, created_at)
            VALUES (:userId, NULL, :skuId, :quantity, now(), 0, now())
            ON CONFLICT (user_id, sku_id) DO UPDATE
            SET quantity = :quantity, version = cart_item.version + 1
            """, nativeQuery = true)
    void upsertMemberQuantity(@Param("userId") Long userId,
                              @Param("skuId") Long skuId,
                              @Param("quantity") int quantity);

//    int updateQuantityWithVersion(Long userId, Long skuId, int quantity, long expectedVersion);
}
