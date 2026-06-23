package com.dawn.cart.dto;

import java.util.List;

public interface CartQueryPort {
    List<CartItemDto> getItems(Long userId);
}
