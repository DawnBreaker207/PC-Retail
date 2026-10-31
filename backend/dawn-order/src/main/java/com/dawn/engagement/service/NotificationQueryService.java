package com.dawn.engagement.service;

import com.dawn.engagement.entity.Notification;
import com.dawn.engagement.infra.persistence.JpaNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationQueryService {

    private final JpaNotificationRepository notificationRepo;

    @Transactional(readOnly = true)
    public List<Notification> listByUser(Long userId) {
        return notificationRepo.findByUserIdOrderByIdDesc(userId);
    }
}
