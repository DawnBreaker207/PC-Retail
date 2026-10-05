package com.dawn.order.port.out;

import com.dawn.order.dto.CartItemSnapshot;

import java.util.List;

public interface CartClientPort {
    List<CartItemSnapshot> getItems(Long userId);

    void clearCart(Long userId);

    List<CartItemSnapshot> getGuestItems(String guestToken);

    void clearGuestCart(String guestToken);
}
