package com.dawn.cart.service;

import com.dawn.common.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class CartCleanupListener {

    private final CartService cartService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Retryable(maxAttempts = 3, retryFor = OptimisticLockingFailureException.class)
    public void onOrderCreated(OrderCreatedEvent event) {
        if (event.userId() == null) return; // guest: đã clear theo token ở checkout
        cartService.clearCart(event.userId());
    }
}
