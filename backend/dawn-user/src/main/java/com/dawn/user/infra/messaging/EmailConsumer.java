package com.dawn.user.infra.messaging;

import com.dawn.user.dto.EmailMessage;
import com.dawn.user.port.out.EmailPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailConsumer {

    private final EmailPort emailPort;

    @RabbitListener(queues = UserRabbitMQConstants.EMAIL_QUEUE)
    public void handleEmail(EmailMessage message) {
        log.debug("Processing email type='{}' to={}", message.type(), message.toEmail());
        try {
            switch (message.type()) {
                case "WELCOME" -> emailPort.sendWelcomeEmail(message.toEmail(), message.recipientName());
                case "OTP_VERIFICATION" ->
                        emailPort.sendOtpVerificationEmail(message.toEmail(), message.recipientName(), message.payload());
                case "PASSWORD_RESET" ->
                        emailPort.sendPasswordResetEmail(message.toEmail(), message.recipientName(), message.payload());
                default -> log.warn("Unknown email type '{}', skipped", message.type());
            }
        } catch (Exception e) {
            log.error("Failed to send email type={} to={}", message.type(), message.toEmail(), e);
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }
}
