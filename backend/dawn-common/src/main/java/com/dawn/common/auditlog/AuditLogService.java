package com.dawn.common.auditlog;

import com.dawn.common.response.ResponsePage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;
    private final tools.jackson.databind.ObjectMapper objectMapper;

    @Autowired
    @Lazy
    private AuditLogService self;

    @Transactional(readOnly = true)
    public ResponsePage<AuditLog> searchLogs(
            LocalDateTime startDate,
            LocalDateTime endDate,
            int page,
            int size) {
        Page<AuditLog> result = auditLogRepository.findAll(AuditLogSpecification.build(startDate, endDate),
                PageRequest.of(page, size, Sort.by("createdAt").descending())
        );
        return ResponsePage.of(
                result.getContent(),
                page,
                size,
                result.getTotalElements());
    }

    @Transactional
    @Scheduled(cron = "0 0 3 * * ?")
    public void autoCleanLogs() {
        LocalDateTime threshold = LocalDateTime.now().minusMonths(6);
        log.info("Starting clean audit log before: {}", threshold);

        auditLogRepository.deleteOlderThan(threshold.toInstant(ZoneOffset.UTC));
        log.info("Auto start clean Audit Log before: {}", threshold);
    }

    /** Dispatcher: Tier 1 đồng bộ + tx riêng; Tier 2 bất đồng bộ best-effort. */
    public void log(String action, String entityType, Long entityId, String oldValue, String newValue, Long actorId, String actorRole, int tier) {
        if (tier == 1) {
            // Tier 1 (refund/dispute): synchronous, separate transaction — survives main tx rollback
            self.writeTier1Log(action, entityType, entityId, oldValue, newValue, actorId, actorRole);
        } else {
            // via proxy so @Async takes effect; direct self-call bypasses the proxy
            self.writeTier2Log(action, entityType, entityId, oldValue, newValue, actorId, actorRole);
        }
    }

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void writeTier1Log(String action, String entityType, Long entityId, String oldValue, String newValue, Long actorId, String actorRole) {
        persist(action, entityType, entityId, oldValue, newValue, actorId, actorRole, 1, "SUCCESS", null);
    }

    @Async
    public void writeTier2Log(String action, String entityType, Long entityId, String oldValue, String newValue, Long actorId, String actorRole) {
        persist(action, entityType, entityId, oldValue, newValue, actorId, actorRole, 2, "SUCCESS", null);
    }

    private void persist(String action, String entityType, Long entityId, String oldValue, String newValue,
                         Long actorId, String actorRole, int tier, String status, String errorMessage) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .oldValue(toJson(oldValue))
                    .newValue(toJson(newValue))
                    .actorId(actorId)
                    .actorRole(actorRole)
                    .tier((short) tier)
                    .status(status)
                    .errorMessage(errorMessage)
                    .build();
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.warn("Audit log save failed (non-critical)", e);
        }
    }

    /** jsonb chỉ nhận JSON hợp lệ — bọc String thường thành chuỗi JSON. */
    private String toJson(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        if (trimmed.startsWith("{") || trimmed.startsWith("[")) return value;
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return "\"\"";
        }
    }
}
