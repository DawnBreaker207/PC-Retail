package com.dawn.user.infra.email;

import com.dawn.user.dto.EmailMessage;
import com.dawn.user.port.out.EmailPort;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Component
@RequiredArgsConstructor
@Slf4j
public class MailtrapAdapter implements EmailPort {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.mail.from:noreply@pcretail.vn}")
    private String fromAddress;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;


    // WELCOME MAIL
    @Override
    public void sendWelcomeEmail(String toEmail, String recipientName) {
        Context ctx = new Context();
        ctx.setVariable("fullName", recipientName);

        sendHtml(toEmail, "Chào mừng đến với PC Retail", "email/welcome", ctx);
    }


    // OPT MAIL
    @Override
    public void sendOtpVerificationEmail(String toEmail, String recipientName, String otp) {
        Context ctx = new Context();
        ctx.setVariable("fullName", recipientName);
        ctx.setVariable("otp", otp);
        ctx.setVariable("ttlMinutes", 5);

        sendHtml(toEmail, "Mã xác thực tài khoản PC Retail", "email/otp-verification", ctx);
    }

    // RESET MAIL
    @Override
    public void sendPasswordResetEmail(String toEmail, String recipientName, String rawToken) {
        Context ctx = new Context();

        ctx.setVariable("fullName", recipientName);
        ctx.setVariable("resetUrl", frontendUrl + "/auth/reset-password?token=" + rawToken);
        ctx.setVariable("ttlMinutes", 15);

        sendHtml(toEmail, "Đặt lại mật khẩu PC Retail", "email/password-reset", ctx);
    }


    private void sendHtml(
            String to,
            String subject,
            String templateName,
            Context ctx) {
        try {
            String html = templateEngine.process(templateName, ctx);

            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);

            mailSender.send(mime);
            log.debug("Email '{}' send to {}", subject, to);
        } catch (MessagingException ex) {
            log.warn("Failed to send email '{}' to {}: {}", subject, to, ex.getMessage());
        }
    }
}
