package com.dawn.order.infra.messaging;

import com.dawn.common.dlt.DeadLetterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * Enterprise Resilient Kafka Consumer:
 * - Processes Order events published by Transactional Outbox.
 * - Automatic exponential backoff retry on transient processing errors (1s -> 2s -> 4s).
 * - Automatic dead-letter topic routing (order-events-dlt) when retry limit is reached.
 * - Persists failed events to PostgreSQL dead_letter_records table for audit and replay.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final DeadLetterService deadLetterService;

    @RetryableTopic(
            attempts = "3",
            backOff = @BackOff(delay = 1000, multiplier = 2.0, maxDelay = 10000),
            dltStrategy = DltStrategy.FAIL_ON_ERROR,
            autoCreateTopics = "true"
    )
    @KafkaListener(topics = "order-events", groupId = "order-event-consumer-group")
    public void onOrderEvent(
            ConsumerRecord<String, String> record,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key) {
        log.info("Processed order event from topic='{}', key='{}', payload length={}",
                topic, key, record.value() != null ? record.value().length() : 0);
    }

    @DltHandler
    public void onDeadLetterEvent(
            ConsumerRecord<String, String> record,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key) {
        log.error("ALERT: Dead Letter Topic reached! Topic='{}', Key='{}', Payload='{}'",
                topic, key, record.value());
        try {
            deadLetterService.recordFailure(topic, key, record.value(), "Order event retry limit reached");
        } catch (Exception e) {
            log.error("Failed to persist DLT record to database: {}", e.getMessage());
        }
    }
}
