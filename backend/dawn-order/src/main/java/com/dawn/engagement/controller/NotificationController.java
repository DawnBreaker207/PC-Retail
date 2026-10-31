package com.dawn.engagement.controller;

import com.dawn.common.security.AuthorizationConstants;
import com.dawn.common.utils.SecurityUtils;
import com.dawn.engagement.entity.Notification;
import com.dawn.engagement.service.NotificationCommandService;
import com.dawn.engagement.service.NotificationQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@PreAuthorize(AuthorizationConstants.CUSTOMER_ONLY)
public class NotificationController {

    private final NotificationQueryService notificationQueryService;
    private final NotificationCommandService notificationCommandService;

    @GetMapping
    public List<Notification> list() {
        return notificationQueryService.listByUser(SecurityUtils.getCurrentUserId());
    }

    @PutMapping("/{id}/read")
    public Notification read(@PathVariable Long id) {
        return notificationCommandService.markRead(id);
    }

    @PutMapping("/read-all")
    public void readAll() {
        notificationCommandService.markAllRead(SecurityUtils.getCurrentUserId());
    }
}
