package com.dawn.inventory.infra.messaging;

import com.dawn.common.dlt.DeadLetterService;
import com.dawn.common.event.InventoryFailedEvent;
import com.dawn.common.event.InventoryLine;
import com.dawn.common.event.InventoryReservedEvent;
import com.dawn.common.event.OrderCreatedEvent;
import com.dawn.common.lock.AdvisoryLocks;
import com.dawn.common.outbox.OutboxPublisher;
import com.dawn.inventory.dto.StockDecrementCommandDto;
import com.dawn.inventory.port.in.ReserveStockUseCase;
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
 * Listens to order-events topic. On OrderCreated, reserves inventory for all order items.
 * On reservation failure (out of stock), publishes InventoryFailedEvent to trigger
 * SAGA compensating transaction: payment refund → order cancel (CANCELLED_OUT_OF_STOCK).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final ReserveStockUseCase reserveStockUseCase;
    private final OutboxPublisher outboxPublisher;
    private final DeadLetterService deadLetterService;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final AdvisoryLocks advisoryLocks;

    @RetryableTopic(
            attempts = "3",
            backOff = @BackOff(delay = 2000, multiplier = 2.0, maxDelay = 20000),
            dltStrategy = DltStrategy.FAIL_ON_ERROR,
            autoCreateTopics = "true"
    )
    @KafkaListener(topics = "order-events", groupId = "inventory-order-consumer-group")
    @Transactional
    public void onOrderEvent(
            ConsumerRecord<String, String> record,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key) {

        String payload = record.value();
        if (payload == null || payload.isBlank()) return;

        try {
            OrderCreatedEvent event = objectMapper.readValue(payload, OrderCreatedEvent.class);
            if (event.orderId() == null) {
                log.debug("Not an OrderCreatedEvent or missing orderId, skipping");
                return;
            }
            // Use advisory lock per orderId bucket to prevent duplicate reservation across replicas
            long lockId = 2000L + (event.orderId() % 100);
            advisoryLocks.runIfLeader(lockId, () -> processReservation(event));
        } catch (Exception e) {
            log.error("Error processing order event in inventory (topic={}, key={}): {}", topic, key, e.getMessage());
            throw new RuntimeException("Inventory order event processing failed, will retry", e);
        }
    }

    private void processReservation(OrderCreatedEvent event) {
        Long orderId = event.orderId();

        // Idempotency check: skip if already reserved
        if (checkAlreadyReserved(orderId)) {
            log.info("Inventory already reserved for orderId={}, skipping (idempotent)", orderId);
            return;
        }

        List<StockDecrementCommandDto> cartItems = queryOrderItems(orderId);
        if (cartItems.isEmpty()) {
            log.warn("No order items found for orderId={}, skipping reservation", orderId);
            return;
        }

        try {
            reserveStockUseCase.reserve(cartItems, orderId);
            log.info("Inventory reserved for orderId={}, skus={}", orderId,
                    cartItems.stream().map(StockDecrementCommandDto::skuId).toList());

            List<InventoryLine> lines = cartItems.stream()
                    .map(c -> new InventoryLine(c.skuId(), c.quantity()))
                    .toList();
            outboxPublisher.publish("inventory-events", "ORDER", orderId,
                    new InventoryReservedEvent(orderId, lines));

        } catch (Exception e) {
            log.error("Inventory reservation failed for orderId={}: {}", orderId, e.getMessage());
            // SAGA compensation: InventoryFailedEvent → payment refund → order cancel
            outboxPublisher.publish("inventory-events", "ORDER", orderId,
                    new InventoryFailedEvent(orderId, e.getMessage(),
                            cartItems.stream().map(StockDecrementCommandDto::skuId).toList()));
        }
    }

    private List<StockDecrementCommandDto> queryOrderItems(Long orderId) {
        return jdbcTemplate.query(
                """
                SELECT oi.sku_id, oi.quantity
                FROM order_item oi
                JOIN sub_order so ON oi.sub_order_id = so.id
                WHERE so.order_id = ?
                """,
                (rs, rowNum) -> new StockDecrementCommandDto(rs.getLong("sku_id"), rs.getInt("quantity")),
                orderId);
    }

    private boolean checkAlreadyReserved(Long orderId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM inventory_transaction WHERE reference_id = ? AND type = 'RESERVE' AND reference_type = 'ORDER_ITEM'",
                Integer.class, orderId);
        return count != null && count > 0;
    }

    @DltHandler
    public void onDeadLetterEvent(
            ConsumerRecord<String, String> record,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key) {
        log.error("ALERT: Inventory order event reached DLT! topic='{}', key='{}', payload='{}'",
                topic, key, record.value());
        try {
            deadLetterService.recordFailure(topic, key, record.value(),
                    "Inventory order reservation retry limit reached — manual intervention required");
        } catch (Exception e) {
            log.error("Failed to persist DLT record: {}", e.getMessage());
        }
    }
}
