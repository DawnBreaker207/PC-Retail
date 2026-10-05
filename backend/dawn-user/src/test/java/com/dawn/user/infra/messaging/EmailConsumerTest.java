package com.dawn.user.infra.messaging;

import com.dawn.user.dto.EmailMessage;
import com.dawn.user.port.out.EmailPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailConsumerTest {

    @Mock
    EmailPort emailPort;

    @InjectMocks
    EmailConsumer consumer;

    @Test
    @DisplayName("3 loại mail đi đúng handler với đủ tham số")
    void routesAllTypes() {
        consumer.handleEmail(EmailMessage.welcome("a@x.vn", "A"));
        consumer.handleEmail(EmailMessage.otpVerification("a@x.vn", "A", "111111"));
        consumer.handleEmail(EmailMessage.passwordReset("a@x.vn", "A", "tok"));

        verify(emailPort).sendWelcomeEmail("a@x.vn", "A");
        verify(emailPort).sendOtpVerificationEmail("a@x.vn", "A", "111111");
        verify(emailPort).sendPasswordResetEmail(eq("a@x.vn"), eq("A"), eq("tok"));
    }

    @Test
    @DisplayName("Type lạ: bỏ qua, không nổ, không requeue")
    void unknownType_skipped() {
        consumer.handleEmail(new EmailMessage("a@x.vn", "SPAM", "A", null));

        verifyNoInteractions(emailPort);
    }

    @Test
    @DisplayName("SMTP lỗi: reject không requeue (tránh poison message)")
    void sendFailure_rejectNoRequeue() {
        doThrow(new RuntimeException("smtp down")).when(emailPort)
                .sendWelcomeEmail(anyString(), anyString());

        assertThatThrownBy(() -> consumer.handleEmail(EmailMessage.welcome("a@x.vn", "A")))
                .isInstanceOf(AmqpRejectAndDontRequeueException.class);
    }
}
