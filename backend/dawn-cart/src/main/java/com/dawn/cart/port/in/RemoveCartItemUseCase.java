package com.dawn.cart.port.in;

public interface RemoveCartItemUseCase {
    void removeItem(Long userId, Long skuId);
}
