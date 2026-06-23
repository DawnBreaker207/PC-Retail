package com.dawn.cart.service;

import com.dawn.cart.port.out.CartCachePort;
import com.dawn.cart.port.out.CartItemRepository;
import com.dawn.common.event.CartUpdateEvent;
import com.dawn.common.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class CartCleanupListener {
    private final CartItemRepository cartItemRepository;
    private final CartCachePort cartCachePort;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCartUpdated(CartUpdateEvent event) {
        cartCachePort.evict(event.userId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderCreated(OrderCreatedEvent event) {
        cartItemRepository.deleteByUserId(event.userId());
        cartCachePort.evict(event.userId());
    }
}
