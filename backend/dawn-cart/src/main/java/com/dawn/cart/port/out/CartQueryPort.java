package com.dawn.cart.port.out;

import com.dawn.cart.dto.CartItemDto;

import java.util.List;

public interface CartQueryPort {
    List<CartItemDto> getItems(Long userId);
}
