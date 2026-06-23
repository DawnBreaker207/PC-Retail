package com.dawn.cart.port.out;

import com.dawn.cart.dto.CartSummaryDto;

import javax.swing.text.html.Option;
import java.util.Optional;

public interface CartCachePort {
    Optional<CartSummaryDto> getCart(Long userId);

    void putCart(Long userId, CartSummaryDto summary);

    void evict(Long userId);

    Optional<Integer> getItemCount(Long userId);

    void putItemCount(Long userId, int count);
}
