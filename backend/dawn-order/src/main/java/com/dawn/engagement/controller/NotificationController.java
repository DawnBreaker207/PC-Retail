package com.dawn.engagement.controller;

import com.dawn.common.security.AuthorizationConstants;
import com.dawn.common.utils.SecurityUtils;
import com.dawn.engagement.entity.Notification;
import com.dawn.engagement.infra.persistence.JpaNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@PreAuthorize(AuthorizationConstants.CUSTOMER_ONLY)
public class NotificationController {

    private final JpaNotificationRepository notificationRepo;

    @GetMapping
    public List<Notification> list() {
        return notificationRepo.findByUserIdOrderByIdDesc(SecurityUtils.getCurrentUserId());
    }

    @PutMapping("/{id}/read")
    public Notification read(@PathVariable Long id) {
        Notification n = notificationRepo.findById(id).orElseThrow();
        n.setRead(true);
        return notificationRepo.save(n);
    }

    @PutMapping("/read-all")
    public void readAll() {
        List<Notification> list = notificationRepo.findByUserIdOrderByIdDesc(SecurityUtils.getCurrentUserId());
        list.forEach(n -> n.setRead(true));
        notificationRepo.saveAll(list);
    }
}
