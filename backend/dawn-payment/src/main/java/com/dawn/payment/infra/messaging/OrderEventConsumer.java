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
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * SAGA compensation: listens to order-events for SubOrderCancelledEvent.
 * When a PAID ONLINE sub-order is cancelled, automatically processes the payment refund.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final PaymentRefundCommandService refundCommandService;
    private final DeadLetterService deadLetterService;
    private final ObjectMapper objectMapper;

    @RetryableTopic(
            attempts = "4",
            backOff = @BackOff(delay = 2000, multiplier = 2.0, maxDelay = 30000),
            dltStrategy = DltStrategy.FAIL_ON_ERROR,
            autoCreateTopics = "true"
    )
    @KafkaListener(topics = "order-events", groupId = "payment-order-consumer-group")
    public void onOrderEvent(
            ConsumerRecord<String, String> record,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key) {
        String payload = record.value();
        if (payload == null || payload.isBlank()) return;

        try {
            Map<String, Object> fields = objectMapper.readValue(payload, new TypeReference<>() {});
            // SubOrderCancelledEvent has subOrderId + orderId + reason (no newStatus, no returnStatus)
            if (!fields.containsKey("subOrderId") || !fields.containsKey("reason")
                    || fields.containsKey("newStatus") || fields.containsKey("returnStatus")) {
                return;
            }

            Long orderId = Long.valueOf(fields.get("orderId").toString());
            String reason = (String) fields.getOrDefault("reason", "sub_order_cancelled");
            refundCommandService.processRefund(orderId, reason);

        } catch (Exception e) {
            log.error("Error processing order event in payment (topic={}, key={}): {}", topic, key, e.getMessage());
            throw new RuntimeException("Payment order event processing failed, will retry", e);
        }
    }

    @DltHandler
    public void onDeadLetterEvent(
            ConsumerRecord<String, String> record,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key) {
        log.error("ALERT: Payment order event reached DLT! topic='{}', key='{}', payload='{}'",
                topic, key, record.value());
        deadLetterService.recordFailure(topic, key, record.value(), "Payment order event retry limit reached");
    }
}
