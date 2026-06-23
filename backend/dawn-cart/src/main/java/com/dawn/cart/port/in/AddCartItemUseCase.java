package com.dawn.cart.port.in;

import com.dawn.cart.dto.CartItemDto;

public interface AddCartItemUseCase {
    CartItemDto addItem(Long userId, Long skuId, int quantity);
}
