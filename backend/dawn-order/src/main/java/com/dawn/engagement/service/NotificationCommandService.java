package com.dawn.engagement.service;

import com.dawn.engagement.entity.Notification;
import com.dawn.engagement.infra.persistence.JpaNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationCommandService {

    private final JpaNotificationRepository notificationRepo;

    @Transactional
    public Notification markRead(Long id) {
        Notification n = notificationRepo.findById(id).orElseThrow();
        n.setRead(true);
        return notificationRepo.save(n);
    }

    @Transactional
    public void markAllRead(Long userId) {
        List<Notification> list = notificationRepo.findByUserIdOrderByIdDesc(userId);
        list.forEach(n -> n.setRead(true));
        notificationRepo.saveAll(list);
    }
}
