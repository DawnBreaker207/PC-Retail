package com.dawn.tax.infra.messaging;

import com.dawn.common.dlt.DeadLetterService;
import com.dawn.tax.service.TaxCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final TaxCommandService taxCommandService;
    private final DeadLetterService deadLetterService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "payment-events", groupId = "dawn-tax")
    public void onPaymentEvent(String payload) {
        try {
            JsonNode node = objectMapper.readTree(payload);
            String status = node.path("status").asText();
            if (!"COMPLETED".equals(status) && !"PAID".equals(status)) {
                return;
            }
            long orderId = node.path("orderId").asLong();
            if (orderId == 0L) {
                log.warn("Payment event missing orderId, skipping");
                return;
            }
            log.info("Payment completed for order {}, generating tax invoice", orderId);
            taxCommandService.generateInvoice(orderId);
        } catch (Exception e) {
            log.error("Failed to process payment event for tax invoice", e);
            deadLetterService.recordFailure("payment-events", null, payload, e.getMessage());
        }
    }
}
