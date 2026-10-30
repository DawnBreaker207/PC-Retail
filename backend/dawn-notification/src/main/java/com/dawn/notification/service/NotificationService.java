package com.dawn.notification.service;

import com.dawn.notification.dto.NotificationEvent;
import com.dawn.notification.entity.NotificationRecord;
import com.dawn.notification.entity.enums.NotificationStatus;
import com.dawn.notification.repository.NotificationRecordRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender mailSender;
    private final NotificationRecordRepository recordRepository;
    private final TemplateRenderer templateRenderer;
    private final ObjectMapper objectMapper;

    @Transactional
    public void processEvent(NotificationEvent event) {
        String variablesJson = serializeVariables(event.variables());

        NotificationRecord record = NotificationRecord.builder()
                .recipientId(event.recipientId())
                .channel(event.channel())
                .templateId(event.templateId())
                .variables(variablesJson)
                .status(NotificationStatus.PENDING)
                .createdAt(Instant.now())
                .build();
        record = recordRepository.save(record);

        try {
            dispatch(event);
            record.setStatus(NotificationStatus.SENT);
            record.setSentAt(Instant.now());
        } catch (Exception e) {
            log.error("Failed to send notification type='{}' recipientId={}: {}", event.type(), event.recipientId(), e.getMessage());
            record.setStatus(NotificationStatus.FAILED);
            record.setErrorMessage(e.getMessage());
        }
        recordRepository.save(record);
    }

    private void dispatch(NotificationEvent event) throws Exception {
        switch (event.channel()) {
            case EMAIL -> {
                String html = templateRenderer.render(event.templateId(), event.variables());
                String subject = resolveSubject(event.templateId(), event.variables());
                sendEmail(event.recipientEmail(), subject, html);
            }
            case SMS -> log.info("SMS channel not yet integrated — skipping recipientPhone={}", event.recipientPhone());
            case PUSH -> log.info("PUSH channel not yet integrated — skipping recipientId={}", event.recipientId());
        }
    }

    public void sendEmail(String to, String subject, String htmlBody) throws Exception {
        MimeMessage msg = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);
        mailSender.send(msg);
        log.debug("Email sent to '{}'", to);
    }

    public Page<NotificationRecord> listByRecipient(Long recipientId, Pageable pageable) {
        return recordRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId, pageable);
    }

    @Scheduled(fixedDelay = 300_000)
    @Transactional
    public void retryFailed() {
        Instant cutoff = Instant.now().minus(24, ChronoUnit.HOURS);
        Page<NotificationRecord> failed = recordRepository.findByStatusAndCreatedAtBefore(
                NotificationStatus.FAILED.name(), cutoff, PageRequest.of(0, 50));

        if (failed.isEmpty()) return;
        log.info("Retrying {} failed notifications", failed.getNumberOfElements());

        for (NotificationRecord record : failed) {
            try {
                Map<String, Object> vars = deserializeVariables(record.getVariables());
                NotificationEvent event = new NotificationEvent(
                        "retry", record.getRecipientId(), null, null,
                        record.getChannel(), record.getTemplateId(), vars);
                dispatch(event);
                record.setStatus(NotificationStatus.SENT);
                record.setSentAt(Instant.now());
                record.setErrorMessage(null);
            } catch (Exception e) {
                log.error("Retry failed for notification id={}: {}", record.getId(), e.getMessage());
                record.setErrorMessage(e.getMessage());
            }
            recordRepository.save(record);
        }
    }

    public Map<String, Long> getStats() {
        long sent = recordRepository.count();
        long failed = recordRepository.findByStatusAndCreatedAtBefore(
                NotificationStatus.FAILED.name(), Instant.now(), PageRequest.of(0, 1)).getTotalElements();
        return Map.of("total", sent, "failed", failed);
    }

    private String serializeVariables(Map<String, Object> vars) {
        if (vars == null) return "{}";
        try {
            return objectMapper.writeValueAsString(vars);
        } catch (Exception e) {
            return "{}";
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> deserializeVariables(String json) {
        if (json == null || json.isBlank()) return Map.of();
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            return Map.of();
        }
    }

    private String resolveSubject(String templateId, Map<String, Object> vars) {
        if (vars != null && vars.containsKey("subject")) {
            return String.valueOf(vars.get("subject"));
        }
        return switch (templateId) {
            case "order-created" -> "Đặt hàng thành công";
            case "payment-success" -> "Thanh toán thành công";
            case "otp-verification" -> "Mã xác thực OTP";
            case "password-reset" -> "Đặt lại mật khẩu";
            case "order-shipped" -> "Đơn hàng đang được giao";
            default -> "Thông báo từ PC-Retail";
        };
    }
}
