package com.dawn.cart.service;

import com.dawn.cart.port.out.CartCachePort;
import com.dawn.cart.port.out.CartItemRepository;
import com.dawn.common.event.OrderCreatedEvent;

import lombok.RequiredArgsConstructor;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class CartCleanupListener {

    private final CartItemRepository cartItemRepository;
    private final CartCachePort cartCachePort;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Retryable(maxAttempts = 3)
    public void onOrderCreated(OrderCreatedEvent event) {
        cartItemRepository.deleteByUserId(event.userId());
        cartCachePort.evict(event.userId());
    }
}
