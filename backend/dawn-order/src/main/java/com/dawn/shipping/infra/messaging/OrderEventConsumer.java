package com.dawn.shipping.infra.messaging;

import com.dawn.common.dlt.DeadLetterService;
import com.dawn.shipping.service.ShippingCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component("shippingOrderEventConsumer")
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final ShippingCommandService shippingCommandService;
    private final DeadLetterService deadLetterService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "order-events", groupId = "dawn-shipping")
    public void onOrderEvent(ConsumerRecord<String, String> record) {
        String payload = record.value();
        try {
            JsonNode node = objectMapper.readTree(payload);
            String type = node.path("type").asText("");
            String status = node.path("status").asText("");

            if (!"AWAITING_SHIPPING".equals(status) && !"ORDER_PLACED".equals(type)) {
                return;
            }

            Long orderId = node.path("orderId").asLong();
            Long userId = node.path("userId").asLong(0L);
            String toAddress = node.path("shippingAddress").asText(node.path("toAddress").asText(""));
            int weightGrams = node.path("totalWeightGrams").asInt(500);

            shippingCommandService.createShippingOrder(orderId, userId > 0 ? userId : null, toAddress, weightGrams);

        } catch (Exception e) {
            log.error("Failed to process order-event for shipping: {}", e.getMessage(), e);
            deadLetterService.recordFailure("order-events", record.key(), payload, e.getMessage());
        }
    }
}
