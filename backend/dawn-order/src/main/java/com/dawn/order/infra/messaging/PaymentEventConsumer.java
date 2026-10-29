package com.dawn.order.infra.messaging;

import com.dawn.common.exception.wrapper.ConflictException;
import com.dawn.common.exception.wrapper.ResourceNotFoundException;
import com.dawn.payment.port.out.OrderClientPort;
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
 * Async fallback for payment → order status transitions.
 *
 * Primary path: QueryPaymentStatusService calls orderClient.markOrderPaid/Failed() via Feign (sync).
 * This consumer handles the case where Feign fails (network blip, transient error).
 * Both paths use CAS (transitionIfStatus) so ConflictException means "already done" — idempotent.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final OrderClientPort orderClientPort;
    private final ObjectMapper objectMapper;

    @RetryableTopic(
            attempts = "4",
            backOff = @BackOff(delay = 2000, multiplier = 2.0, maxDelay = 30000),
            dltStrategy = DltStrategy.FAIL_ON_ERROR,
            autoCreateTopics = "true"
    )
    @KafkaListener(topics = "payment-events", groupId = "order-payment-consumer-group")
    public void onPaymentEvent(
            ConsumerRecord<String, String> record,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key) {

        String payload = record.value();
        if (payload == null || payload.isBlank()) {
            log.warn("Received empty payment event: topic='{}', key='{}'", topic, key);
            return;
        }

        try {
            Map<String, Object> fields = objectMapper.readValue(payload, new TypeReference<>() {});
            Object rawOrderId = fields.get("orderId");
            Object rawPaymentId = fields.get("paymentId");
            if (rawOrderId == null || rawPaymentId == null) {
                log.error("Malformed payment event (missing orderId/paymentId): topic='{}', key='{}', payload='{}'",
                        topic, key, payload);
                return;
            }
            long orderId = ((Number) rawOrderId).longValue();
            long paymentId = ((Number) rawPaymentId).longValue();

            if (fields.containsKey("transactionRef")) {
                orderClientPort.markOrderPaid(orderId);
                log.info("Async PAID: orderId={} paymentId={}", orderId, paymentId);
            } else if (fields.containsKey("reason")) {
                orderClientPort.markOrderPaymentFailed(orderId);
                log.info("Async FAILED: orderId={} paymentId={} reason='{}'",
                        orderId, paymentId, fields.get("reason"));
            } else {
                log.warn("Unknown payment event shape: topic='{}', key='{}': {}", topic, key, payload);
            }
        } catch (ConflictException e) {
            // CAS conflict: Feign already transitioned the order — idempotent, do not retry
            log.info("Idempotent skip (already transitioned): key='{}', {}", key, e.getMessage());
        } catch (ResourceNotFoundException e) {
            // Order not found — permanent failure, do not retry
            log.error("Order not found during async payment event processing: key='{}', {}", key, e.getMessage());
        } catch (Exception e) {
            log.error("Failed to process payment event: topic='{}', key='{}': {}", topic, key, e.getMessage(), e);
            throw new RuntimeException("Payment event processing failed, will retry", e);
        }
    }

    @DltHandler
    public void onDeadLetterEvent(
            ConsumerRecord<String, String> record,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key) {
        log.error("ALERT: Payment event reached Dead Letter Topic! topic='{}', key='{}', payload='{}'",
                topic, key, record.value());
    }
}
