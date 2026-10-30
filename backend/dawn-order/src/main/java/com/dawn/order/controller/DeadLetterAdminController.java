package com.dawn.order.controller;

import com.dawn.common.dlt.DeadLetterRecord;
import com.dawn.common.dlt.DeadLetterService;
import com.dawn.common.security.AuthorizationConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin/dlt")
@RequiredArgsConstructor
public class DeadLetterAdminController {

    private final DeadLetterService deadLetterService;

    @GetMapping
    @PreAuthorize(AuthorizationConstants.ADMIN_ONLY)
    public ResponseEntity<List<DeadLetterRecord>> listPending() {
        return ResponseEntity.ok(deadLetterService.listPending());
    }

    @PostMapping("/{id}/replay")
    @PreAuthorize(AuthorizationConstants.ADMIN_ONLY)
    public ResponseEntity<Map<String, Object>> replay(@PathVariable Long id) {
        log.info("Admin initiated DLT replay for record id={}", id);
        deadLetterService.replay(id);
        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "DLT event replayed successfully to Kafka broker",
                "recordId", id
        ));
    }
}
