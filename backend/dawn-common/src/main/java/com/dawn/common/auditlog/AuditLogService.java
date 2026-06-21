package com.dawn.common.auditlog;

import com.dawn.common.response.ResponsePage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;

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
    public void autoCleanLogs() {
        LocalDateTime threshold = LocalDateTime.now().minusMonths(6);
        log.info("Starting clean audit log before: {}", threshold);

        auditLogRepository.deleteOlderThan(Instant.from(threshold));
        log.info("Auto start clean Audit Log before: {}", threshold);
    }
}
