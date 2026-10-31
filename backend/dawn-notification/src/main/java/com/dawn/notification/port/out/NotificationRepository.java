package com.dawn.notification.port.out;

import com.dawn.notification.entity.NotificationRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;

public interface NotificationRepository {

    Page<NotificationRecord> findByRecipientIdOrderByCreatedAtDesc(Long recipientId, Pageable pageable);

    Page<NotificationRecord> findByStatusAndCreatedAtBefore(String status, Instant before, Pageable pageable);

    long count();
}
