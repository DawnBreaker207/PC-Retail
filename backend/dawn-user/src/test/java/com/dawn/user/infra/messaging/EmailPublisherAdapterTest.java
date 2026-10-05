package com.dawn.user.infra.messaging;

import com.dawn.user.dto.EmailMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailPublisherAdapterTest {

    @Mock
    RabbitTemplate rabbitTemplate;

    @Test
    @DisplayName("Publish đi đúng exchange + routing key, không cần broker thật")
    void publish_routesCorrectly() {
        EmailPublisherAdapter adapter = new EmailPublisherAdapter(rabbitTemplate);
        EmailMessage msg = EmailMessage.passwordReset("a@x.vn", "A", "tok123");

        adapter.publish(msg);

        verify(rabbitTemplate).convertAndSend(
                eq(UserRabbitMQConstants.EMAIL_EXCHANGE),
                eq(UserRabbitMQConstants.EMAIL_ROUTING_KEY),
                eq(msg));
    }

    @Test
    @DisplayName("OTP message mang đúng mã trong payload")
    void otpMessage_carriesCode() {
        EmailMessage msg = EmailMessage.otpVerification("a@x.vn", "A", "654321");

        EmailPublisherAdapter adapter = new EmailPublisherAdapter(rabbitTemplate);
        adapter.publish(msg);

        verify(rabbitTemplate).convertAndSend(anyString(), anyString(),
                argThat((EmailMessage m) -> "654321".equals(m.payload())
                        && "OTP_VERIFICATION".equals(m.type())));
    }
}
