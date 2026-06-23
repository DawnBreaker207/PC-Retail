package com.dawn.cart.port.in;

import com.dawn.cart.dto.CartItemDto;

public interface UpdateCartItemUseCase {
    CartItemDto updateItem(Long userId, Long skuId, int quantity);
}
