package com.dawn.user.infra.messaging;

import com.dawn.user.dto.EmailMessage;
import com.dawn.user.port.out.EmailPublisherPort;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailPublisherAdapter implements EmailPublisherPort {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publish(EmailMessage message) {
        rabbitTemplate.convertAndSend(
                UserRabbitMQConstants.EMAIL_EXCHANGE,
                UserRabbitMQConstants.EMAIL_ROUTING_KEY,
                message);
    }
}
