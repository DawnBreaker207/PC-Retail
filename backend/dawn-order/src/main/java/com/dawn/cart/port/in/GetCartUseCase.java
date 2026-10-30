package com.dawn.cart.port.in;

import com.dawn.cart.dto.CartSummaryDto;

public interface GetCartUseCase {
    CartSummaryDto getCart(Long userId);

    int getItemCount(Long userId);
}
