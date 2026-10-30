package com.dawn.inventory.infra.messaging;

import com.dawn.common.dlt.DeadLetterService;
import com.dawn.common.event.InventoryFailedEvent;
import com.dawn.common.event.PaymentCompletedEvent;
import com.dawn.common.outbox.OutboxPublisher;
import com.dawn.inventory.dto.StockDecrementCommandDto;
import com.dawn.inventory.port.in.DeductStockUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * Listens to payment-events. On PaymentCompleted, deducts inventory (converts reserved → deducted).
 * On deduction failure, publishes InventoryFailedEvent to trigger payment refund (SAGA compensation).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final DeductStockUseCase deductStockUseCase;
    private final OutboxPublisher outboxPublisher;
    private final DeadLetterService deadLetterService;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    @RetryableTopic(
            attempts = "3",
            backOff = @BackOff(delay = 2000, multiplier = 2.0, maxDelay = 20000),
            dltStrategy = DltStrategy.FAIL_ON_ERROR,
            autoCreateTopics = "true"
    )
    @KafkaListener(topics = "payment-events", groupId = "inventory-payment-consumer-group")
    @Transactional
    public void onPaymentEvent(
            ConsumerRecord<String, String> record,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key) {
        String payload = record.value();
        if (payload == null || payload.isBlank()) return;

        try {
            PaymentCompletedEvent event = objectMapper.readValue(payload, PaymentCompletedEvent.class);
            if (event.orderId() == null || event.transactionRef() == null) {
                log.debug("Not a PaymentCompletedEvent, skipping");
                return;
            }
            processDeduction(event);
        } catch (Exception e) {
            log.error("Error processing payment event in inventory (topic={}, key={}): {}", topic, key, e.getMessage());
            throw new RuntimeException("Inventory payment event processing failed, will retry", e);
        }
    }

    private void processDeduction(PaymentCompletedEvent event) {
        Long orderId = event.orderId();
        List<StockDecrementCommandDto> reservedItems = queryReservedItems(orderId);

        if (reservedItems.isEmpty()) {
            log.warn("No reserved items found for orderId={}, skipping deduction", orderId);
            return;
        }

        List<Long> failedSkuIds = List.of();
        try {
            deductStockUseCase.deduct(reservedItems, orderId);
            log.info("Inventory deducted for orderId={}, skus={}", orderId,
                    reservedItems.stream().map(StockDecrementCommandDto::skuId).toList());
        } catch (Exception e) {
            failedSkuIds = reservedItems.stream().map(StockDecrementCommandDto::skuId).toList();
            log.error("Inventory deduction failed for orderId={}: {}", orderId, e.getMessage());
            outboxPublisher.publish("inventory-events", "ORDER", orderId,
                    new InventoryFailedEvent(orderId, e.getMessage(), failedSkuIds));
        }
    }

    private List<StockDecrementCommandDto> queryReservedItems(Long orderId) {
        return jdbcTemplate.query(
                "SELECT sku_id, quantity FROM inventory_transaction WHERE reference_id = ? AND type = 'RESERVE' AND reference_type = 'ORDER_ITEM'",
                (rs, rowNum) -> new StockDecrementCommandDto(rs.getLong("sku_id"), rs.getInt("quantity")),
                orderId);
    }

    @DltHandler
    public void onDeadLetterEvent(
            ConsumerRecord<String, String> record,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key) {
        log.error("ALERT: Inventory payment event reached DLT! topic='{}', key='{}', payload='{}'",
                topic, key, record.value());
        try {
            deadLetterService.recordFailure(topic, key, record.value(), "Inventory payment event retry limit reached");
        } catch (Exception e) {
            log.error("Failed to persist DLT record: {}", e.getMessage());
        }
    }
}
