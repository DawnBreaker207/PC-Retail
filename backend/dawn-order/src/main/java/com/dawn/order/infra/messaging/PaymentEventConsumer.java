package com.dawn.order.infra.messaging;

import com.dawn.common.dlt.DeadLetterService;
import com.dawn.common.exception.wrapper.ConflictException;
import com.dawn.common.exception.wrapper.ResourceNotFoundException;
import com.dawn.common.contract.OrderClientPort;
import com.dawn.order.entity.enums.SubOrderStatus;
import com.dawn.order.port.out.SubOrderRepository;
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
 * Async fallback for payment -> order status transitions.
 *
 * Primary path: QueryPaymentStatusService calls orderClient.markOrderPaid/Failed() via Feign (sync).
 * This consumer handles the case where Feign fails (network blip, transient error).
 * Both paths use CAS (transitionIfStatus) so ConflictException means "already done" - idempotent.
 * Persists exhausted failures into dead_letter_records for operational audit and replay.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final OrderClientPort orderClientPort;
    private final SubOrderRepository subOrderRepository;
    private final ObjectMapper objectMapper;
    private final DeadLetterService deadLetterService;

    @RetryableTopic(
            attempts = "4",
            backOff = @BackOff(delay = 2000, multiplier = 2.0, maxDelay = 30000),
            dltStrategy = DltStrategy.FAIL_ON_ERROR,
            autoCreateTopics = "true"
    )
    @KafkaListener(topics = "payment-events", groupId = "order-payment-consumer-group")
    @Transactional
    public void onPaymentEvent(
            ConsumerRecord<String, String> record,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key) {
        String payload = record.value();
        if (payload == null || payload.isBlank()) return;

        try {
            Map<String, Object> fields = objectMapper.readValue(payload, new TypeReference<>() {});
            Object orderIdObj = fields.get("orderId");
            if (orderIdObj == null) {
                log.warn("Payment event missing orderId: {}", payload);
                return;
            }
            Long orderId = Long.valueOf(orderIdObj.toString());

            if (fields.containsKey("transactionRef") && fields.containsKey("paymentId")) {
                // PaymentCompletedEvent (has paymentId + transactionRef + settledAt)
                String transactionRef = (String) fields.get("transactionRef");
                log.info("Kafka consumer: marking order {} paid (ref={})", orderId, transactionRef);
                orderClientPort.markOrderPaid(orderId);
            } else if (fields.containsKey("transactionRef") && !fields.containsKey("paymentId")) {
                // PaymentRefundedEvent (SAGA compensation: inventory failed → payment refunded → cancel order)
                log.warn("SAGA compensation: cancelling order {} as CANCELLED_OUT_OF_STOCK after refund", orderId);
                cancelSubOrdersOutOfStock(orderId);
            } else if (fields.containsKey("reason")) {
                // PaymentFailedEvent
                String reason = (String) fields.get("reason");
                log.info("Kafka consumer: marking order {} payment failed (reason={})", orderId, reason);
                orderClientPort.markOrderPaymentFailed(orderId);
            } else {
                log.debug("Ignoring payment event without recognized discriminator: {}", payload);
            }
        } catch (ConflictException e) {
            // Already in target state (Feign path succeeded first) — idempotent skip
            log.info("Order status already updated (idempotent skip): {}", e.getMessage());
        } catch (ResourceNotFoundException e) {
            log.warn("Order not found during payment event processing: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Error processing payment event (topic={}, key={}): {}", topic, key, e.getMessage());
            throw new RuntimeException("Payment event processing failed, will retry", e);
        }
    }

    private void cancelSubOrdersOutOfStock(Long orderId) {
        subOrderRepository.findByOrderId(orderId).forEach(sub -> {
            int updated = subOrderRepository.transitionIfStatus(
                    sub.getId(), sub.getStatus(), SubOrderStatus.CANCELLED_OUT_OF_STOCK);
            if (updated == 0) {
                log.info("SubOrder {} already transitioned, skipping (idempotent)", sub.getId());
            }
        });
    }

    @DltHandler
    public void onDeadLetterEvent(
            ConsumerRecord<String, String> record,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key) {
        log.error("ALERT: Payment event reached Dead Letter Topic! topic='{}', key='{}', payload='{}'",
                topic, key, record.value());
        try {
            deadLetterService.recordFailure(topic, key, record.value(), "Payment event retry limit reached");
        } catch (Exception e) {
            log.error("Failed to persist DLT record to database: {}", e.getMessage());
        }
    }
}
