package com.dawn.order.infra.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/** Fire-and-forget Kafka publisher for notification events. Wraps qualifier injection. */
@Slf4j
@Component
public class NotificationEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public NotificationEventPublisher(
            @Qualifier("stringKafkaTemplate") KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(String topic, String key, String payload) {
        try {
            kafkaTemplate.send(topic, key, payload);
        } catch (Exception e) {
            log.warn("Notification event send failed topic={} key={}: {}", topic, key, e.getMessage());
        }
    }
}
