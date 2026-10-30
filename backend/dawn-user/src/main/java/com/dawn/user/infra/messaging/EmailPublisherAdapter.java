package com.dawn.user.infra.messaging;

import com.dawn.user.dto.EmailMessage;
import com.dawn.user.port.out.EmailPublisherPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailPublisherAdapter implements EmailPublisherPort {

    private static final String NOTIFICATION_TOPIC = "notification-events";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void publish(EmailMessage message) {
        try {
            String payload = objectMapper.writeValueAsString(message);
            kafkaTemplate.send(NOTIFICATION_TOPIC, message.toEmail(), payload);
            log.debug("Published email notification to Kafka: type={}, to={}", message.type(), message.toEmail());
        } catch (Exception e) {
            log.error("Failed to publish email notification to Kafka: type={}, to={}", message.type(), message.toEmail(), e);
            throw new RuntimeException("Failed to publish email notification", e);
        }
    }
}
