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
public class NotificationPushService {

    private final JpaNotificationRepository notificationRepo;
    private final SimpMessagingTemplate messaging;

    // Sau checkout commit: lưu DB (để offline pull) + đẩy WS realtime.
    // REQUIRES_NEW: AFTER_COMMIT chạy khi tx cha đã commit xong — không có tx mới thì
    // save() bám vào connection zombie, persist treo không flush, mất row mà không lỗi (giống CartCleanupListener)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderCreated(OrderCreatedEvent event) {
        if (event.userId() == null) return; // guest: không có user để push
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
            // WS rớt không được làm hỏng flow — client pull DB khi reconnect
            log.warn("WebSocket push failed for user {}", event.userId(), e);
        }
    }
}
