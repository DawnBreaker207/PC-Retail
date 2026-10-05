package com.dawn.common.admin;

import com.dawn.common.auditlog.AuditLog;
import com.dawn.common.auditlog.AuditLogService;
import com.dawn.common.response.ResponsePage;
import com.dawn.common.security.AuthorizationConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/admin/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    @PreAuthorize(AuthorizationConstants.ADMIN_ONLY)
    public ResponsePage<AuditLog> search(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return auditLogService.searchLogs(from, to, page, size);
    }
}
