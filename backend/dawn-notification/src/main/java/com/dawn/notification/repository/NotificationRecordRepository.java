package com.dawn.notification.repository;

import com.dawn.notification.entity.NotificationRecord;
import com.dawn.notification.port.out.NotificationRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRecordRepository extends JpaRepository<NotificationRecord, Long>, NotificationRepository {
}
