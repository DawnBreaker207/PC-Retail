package com.dawn.user.infra.messaging;

import com.dawn.user.dto.EmailMessage;
import com.dawn.user.port.out.EmailPort;
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
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailConsumer {

    private final EmailPort emailPort;
    private final ObjectMapper objectMapper;

    @RetryableTopic(
            attempts = "3",
            backOff = @BackOff(delay = 2000, multiplier = 2.0, maxDelay = 10000),
            dltStrategy = DltStrategy.FAIL_ON_ERROR,
            autoCreateTopics = "true"
    )
    @KafkaListener(topics = "notification-events", groupId = "email-consumer-group")
    public void handleEmail(ConsumerRecord<String, String> record) {
        String payload = record.value();
        if (payload == null || payload.isBlank()) return;

        try {
            EmailMessage message = objectMapper.readValue(payload, EmailMessage.class);
            log.debug("Processing email type='{}' to={}", message.type(), message.toEmail());
            switch (message.type()) {
                case "WELCOME" -> emailPort.sendWelcomeEmail(message.toEmail(), message.recipientName());
                case "OTP_VERIFICATION" ->
                        emailPort.sendOtpVerificationEmail(message.toEmail(), message.recipientName(), message.payload());
                case "PASSWORD_RESET" ->
                        emailPort.sendPasswordResetEmail(message.toEmail(), message.recipientName(), message.payload());
                default -> log.warn("Unknown email type '{}', skipped", message.type());
            }
        } catch (Exception e) {
            log.error("Failed to process email notification: {}", e.getMessage(), e);
            throw new RuntimeException("Email processing failed, will retry", e);
        }
    }

    @DltHandler
    public void onDeadLetterEvent(
            ConsumerRecord<String, String> record,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key) {
        log.error("ALERT: Email notification reached Dead Letter Topic! topic='{}', key='{}', payload='{}'",
                topic, key, record.value());
    }
}
