package com.dawn.notification.service;

import com.dawn.notification.entity.NotificationRecord;
import com.dawn.notification.entity.enums.NotificationStatus;
import com.dawn.notification.port.out.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationQueryService {

    private final NotificationRepository recordRepository;

    @Transactional(readOnly = true)
    public Page<NotificationRecord> listByRecipient(Long recipientId, Pageable pageable) {
        return recordRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId, pageable);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getStats() {
        long sent = recordRepository.count();
        long failed = recordRepository.findByStatusAndCreatedAtBefore(
                NotificationStatus.FAILED.name(), Instant.now(), PageRequest.of(0, 1)).getTotalElements();
        return Map.of("total", sent, "failed", failed);
    }
}
