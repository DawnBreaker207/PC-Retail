package com.dawn.engagement.service;

import com.dawn.common.event.OrderCreatedEvent;
import com.dawn.engagement.entity.Notification;
import com.dawn.engagement.infra.persistence.JpaNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationPushCommandService {

    private final JpaNotificationRepository notificationRepo;
    private final SimpMessagingTemplate messaging;

    // After checkout commit: persist for offline pull + push via WebSocket.
    // REQUIRES_NEW: AFTER_COMMIT runs outside the parent tx; without it save() may silently not flush
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderCreated(OrderCreatedEvent event) {
        if (event.userId() == null) return; // no user to push for guest orders
        Notification saved = notificationRepo.save(Notification.builder()
                .userId(event.userId())
                .type("ORDER_CREATED")
                .title("Đã nhận đơn hàng #" + event.orderId())
                .body("Đơn hàng của bạn đang chờ thanh toán")
                .referenceType("ORDER")
                .referenceId(event.orderId())
                .build());
        try {
            messaging.convertAndSend("/topic/notifications/" + event.userId(), saved);
        } catch (Exception e) {
            // WebSocket failure must not break the flow; client pulls DB on reconnect
            log.warn("WebSocket push failed for user {}", event.userId(), e);
        }
    }
}
