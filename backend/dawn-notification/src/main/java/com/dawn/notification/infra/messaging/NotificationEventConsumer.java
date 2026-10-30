package com.dawn.notification.infra.messaging;

import com.dawn.common.dlt.DeadLetterService;
import com.dawn.notification.dto.NotificationEvent;
import com.dawn.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventConsumer {

    private static final String TOPIC = "notification-events";

    private final NotificationService notificationService;
    private final DeadLetterService deadLetterService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = TOPIC, groupId = "dawn-notification")
    public void consume(String payload) {
        try {
            NotificationEvent event = objectMapper.readValue(payload, NotificationEvent.class);
            log.debug("Received notification event type='{}' channel={} recipientId={}", event.type(), event.channel(), event.recipientId());
            notificationService.processEvent(event);
        } catch (Exception e) {
            log.error("Failed to process notification event: {}", e.getMessage(), e);
            deadLetterService.recordFailure(TOPIC, null, payload, e.getMessage());
        }
    }
}
