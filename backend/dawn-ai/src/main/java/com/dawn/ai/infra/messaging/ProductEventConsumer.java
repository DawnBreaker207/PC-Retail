package com.dawn.ai.infra.messaging;

import com.dawn.ai.port.out.EmbeddingPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Consumes product-events and triggers SKU re-embedding when catalog changes.
 * Uses EmbeddingPort.generateAndSave which fetches product data and persists the vector.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventConsumer {

    private final EmbeddingPort embeddingPort;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "product-events", groupId = "dawn-ai-product-events")
    public void onProductEvent(String payload) {
        try {
            JsonNode node = objectMapper.readTree(payload);
            long skuId = node.path("skuId").asLong(0);
            if (skuId == 0) {
                log.warn("Skipping product-event with missing skuId");
                return;
            }
            embeddingPort.generateAndSave(skuId);
            log.debug("Re-embedded SKU {}", skuId);
        } catch (Exception e) {
            log.warn("Failed to process product-event for embedding: {}", e.getMessage());
        }
    }
}
