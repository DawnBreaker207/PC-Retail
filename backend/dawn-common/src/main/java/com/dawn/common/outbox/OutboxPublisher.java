package com.dawn.common.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

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
        outboxRepository.save(OutboxEvent.builder()
                .topic(topic)
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .eventType(event.getClass().getSimpleName())
                .payload(objectMapper.writeValueAsString(event))
                .build());
    }
}
