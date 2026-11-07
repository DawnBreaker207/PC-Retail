package com.dawn.payment.infra.messaging;

import com.dawn.common.dlt.DeadLetterService;
import com.dawn.payment.service.PaymentRefundCommandService;
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
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * SAGA compensation: listens to inventory-events for InventoryFailedEvent.
 * Processes refund for the affected order and publishes PaymentRefundedEvent.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryEventConsumer {

    private final PaymentRefundCommandService refundCommandService;
    private final DeadLetterService deadLetterService;
    private final ObjectMapper objectMapper;

    @RetryableTopic(
            attempts = "4",
            backOff = @BackOff(delay = 2000, multiplier = 2.0, maxDelay = 30000),
            dltStrategy = DltStrategy.FAIL_ON_ERROR,
            autoCreateTopics = "true"
    )
    @KafkaListener(topics = "inventory-events", groupId = "payment-inventory-consumer-group")
    @Transactional
    public void onInventoryEvent(
            ConsumerRecord<String, String> record,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key) {
        String payload = record.value();
        if (payload == null || payload.isBlank()) return;

        try {
            Map<String, Object> fields = objectMapper.readValue(payload, new TypeReference<>() {});
            if (!fields.containsKey("failedSkuIds")) {
                log.debug("Not an InventoryFailedEvent, skipping");
                return;
            }

            Long orderId = Long.valueOf(fields.get("orderId").toString());
            String reason = (String) fields.getOrDefault("reason", "inventory_failure");
            refundCommandService.processRefund(orderId, reason);
        } catch (Exception e) {
            log.error("Error processing inventory event in payment (topic={}, key={}): {}", topic, key, e.getMessage());
            throw new RuntimeException("Payment inventory event processing failed, will retry", e);
        }
    }

    @DltHandler
    public void onDeadLetterEvent(
            ConsumerRecord<String, String> record,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key) {
        log.error("ALERT: Payment inventory event reached DLT! topic='{}', key='{}', payload='{}'",
                topic, key, record.value());
        try {
            deadLetterService.recordFailure(topic, key, record.value(), "Payment inventory event retry limit reached");
        } catch (Exception e) {
            log.error("Failed to persist DLT record: {}", e.getMessage());
        }
    }
}
