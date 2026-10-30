package com.dawn.notification.controller;

import com.dawn.common.response.ResponseObject;
import com.dawn.common.response.ResponsePage;
import com.dawn.notification.entity.NotificationRecord;
import com.dawn.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
public class NotificationAdminController {

    private final NotificationService notificationService;

    @GetMapping("/history")
    public ResponseObject<ResponsePage<NotificationRecord>> history(
            @RequestParam Long recipientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseObject.success(
                ResponsePage.of(notificationService.listByRecipient(recipientId, PageRequest.of(page, size))));
    }

    @PostMapping("/retry")
    public ResponseObject<Void> retry() {
        notificationService.retryFailed();
        return ResponseObject.success(null);
    }

    @GetMapping("/stats")
    public ResponseObject<Map<String, Long>> stats() {
        return ResponseObject.success(notificationService.getStats());
    }
}
