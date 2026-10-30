package com.dawn.notification.repository;

import com.dawn.notification.entity.NotificationRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface NotificationRecordRepository extends JpaRepository<NotificationRecord, Long> {

    Page<NotificationRecord> findByRecipientIdOrderByCreatedAtDesc(Long recipientId, Pageable pageable);

    Page<NotificationRecord> findByStatusAndCreatedAtBefore(String status, Instant before, Pageable pageable);
}
