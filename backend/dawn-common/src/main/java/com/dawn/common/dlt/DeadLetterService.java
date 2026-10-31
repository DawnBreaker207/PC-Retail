package com.dawn.common.dlt;

import com.dawn.common.util.AfterCommit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
public class DeadLetterService {
    private final DeadLetterRepository deadLetterRepository;
    @SuppressWarnings("rawtypes")
    @Autowired(required = false)
    private KafkaTemplate kafkaTemplate;

    public DeadLetterService(DeadLetterRepository deadLetterRepository) {
        this.deadLetterRepository = deadLetterRepository;
    }

    @Transactional
    public DeadLetterRecord recordFailure(String topic, String key, String payload, String exceptionMessage) {
        log.error("Recording DLT failure: topic='{}', key='{}', error='{}'", topic, key, exceptionMessage);
        DeadLetterRecord record = DeadLetterRecord.builder()
                .topic(topic)
                .messageKey(key)
                .payload(payload != null ? payload : "")
                .exceptionMessage(exceptionMessage)
                .status("PENDING_REVIEW")
                .createdAt(Instant.now())
                .build();
        return deadLetterRepository.save(record);
    }

    public List<DeadLetterRecord> listPending() {
        return deadLetterRepository.findByStatusOrderByCreatedAtDesc("PENDING_REVIEW");
    }

    @Transactional
    @SuppressWarnings("unchecked")
    public void replay(Long id) {
        DeadLetterRecord record = deadLetterRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("DLT record not found with id: " + id));
        log.info("Replaying DLT record id={}, original topic='{}', key='{}'", id, record.getTopic(), record.getMessageKey());
        if (kafkaTemplate == null) {
            throw new IllegalStateException("KafkaTemplate not available for replay");
        }
        AfterCommit.run(() ->
            kafkaTemplate.send(record.getTopic(), record.getMessageKey(), record.getPayload())
        );
        record.setStatus("REPLAYED");
        record.setReplayedAt(Instant.now());
        deadLetterRepository.save(record);
    }
}
