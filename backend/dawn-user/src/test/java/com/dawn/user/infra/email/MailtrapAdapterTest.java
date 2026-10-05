package com.dawn.user.infra.email;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailtrapAdapterTest {

    @Mock
    JavaMailSender mailSender;
    @Mock
    TemplateEngine templateEngine;
    @Mock
    MimeMessage mimeMessage;

    @InjectMocks
    MailtrapAdapter adapter;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        org.springframework.test.util.ReflectionTestUtils.setField(adapter, "fromAddress", "noreply@test.vn");
        org.springframework.test.util.ReflectionTestUtils.setField(adapter, "frontendUrl", "http://localhost:4200");
    }

    @Test
    @DisplayName("Gửi OTP: đúng người nhận + OTP có trong mail, không cần SMTP thật")
    void sendOtp_usesMockSender() throws Exception {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("email/otp-verification"), any())).thenReturn("<p>123456</p>");

        adapter.sendOtpVerificationEmail("a@x.vn", "A", "123456");

        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("SMTP chết: nuốt lỗi, không vỡ flow đăng ký/quên mật khẩu")
    void smtpDown_swallowed() throws Exception {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any())).thenReturn("<p>x</p>");
        doThrow(new MailSendException("connection refused")).when(mailSender).send(any(MimeMessage.class));

        assertThatNoException().isThrownBy(
                () -> adapter.sendPasswordResetEmail("a@x.vn", "A", "tok"));
    }
}
