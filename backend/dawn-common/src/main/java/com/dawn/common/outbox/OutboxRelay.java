package com.dawn.common.outbox;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Đọc outbox chưa publish và đẩy Kafka. At-least-once: gửi xong mới set publishedAt.
 * Sử dụng SELECT ... FOR UPDATE SKIP LOCKED để nhiều instance scale ngang không bao giờ bị đụng độ.
 */
@Component
@Slf4j
@ConditionalOnProperty(name = "app.outbox.relay.enabled", havingValue = "true", matchIfMissing = true)
public class OutboxRelay {

    private final OutboxRepository outboxRepository;
    private final String bootstrapServers;
    private KafkaProducer<String, String> producer;

    public OutboxRelay(
            OutboxRepository outboxRepository,
            @Value("${spring.kafka.bootstrap-servers:${KAFKA_BOOTSTRAP_SERVERS:localhost:29092}}")
            String bootstrapServers) {
        this.outboxRepository = outboxRepository;
        this.bootstrapServers = bootstrapServers;
    }

    @Transactional
    @Scheduled(fixedDelayString = "${app.outbox.relay.fixed-delay-ms:1000}")
    public void relay() {
        List<OutboxEvent> pending = outboxRepository.findPendingEventsWithLock(100);
        if (pending.isEmpty()) {
            return;
        }
        for (OutboxEvent event : pending) {
            try {
                producer().send(new ProducerRecord<>(
                        event.getTopic(),
                        event.getAggregateType() + ":" + event.getAggregateId(),
                        event.getPayload())).get(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                event.setAttempts(event.getAttempts() + 1);
                outboxRepository.save(event);
                // stop batch to preserve order; retry on next run
                log.warn("Outbox relay paused at id={}: {}", event.getId(), e.getMessage());
                return;
            }
            event.setPublishedAt(Instant.now());
            event.setAttempts(event.getAttempts() + 1);
            outboxRepository.save(event);
        }
    }

    @Transactional
    @Scheduled(cron = "${app.outbox.relay.cleanup-cron:0 0 3 * * ?}")
    public void cleanupOldPublishedEvents() {
        Instant threshold = Instant.now().minus(Duration.ofDays(7));
        int deleted = outboxRepository.deleteOldPublishedEvents(threshold);
        if (deleted > 0) {
            log.info("Outbox relay cleaned up {} old published events older than 7 days", deleted);
        }
    }

    private KafkaProducer<String, String> producer() {
        if (producer == null) {
            Properties props = new Properties();
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            props.put(ProducerConfig.ACKS_CONFIG, "all");
            props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
            props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, "2000");
            props.put(ProducerConfig.CLIENT_ID_CONFIG, "dawn-outbox-relay");
            producer = new KafkaProducer<>(props);
        }
        return producer;
    }

    @jakarta.annotation.PreDestroy
    public void close() {
        if (producer != null) {
            producer.flush();
            producer.close(Duration.ofSeconds(2));
            producer = null;
        }
    }
}
