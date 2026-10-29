package com.dawn.common.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ghi event vào outbox trong đúng transaction nghiệp vụ (MANDATORY: gọi ngoài tx sẽ fail to,
 * tránh dual-write DB + Kafka).
 */
@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.MANDATORY)
    public void publish(String topic, String aggregateType, Long aggregateId, Object event) {
        try {
            outboxRepository.save(OutboxEvent.builder()
                    .topic(topic)
                    .aggregateType(aggregateType)
                    .aggregateId(aggregateId)
                    .eventType(event.getClass().getSimpleName())
                    .payload(objectMapper.writeValueAsString(event))
                    .build());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize outbox event " + event.getClass().getSimpleName(), e);
        }
    }
}
