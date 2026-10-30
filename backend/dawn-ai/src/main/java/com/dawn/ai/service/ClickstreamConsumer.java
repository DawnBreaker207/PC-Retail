package com.dawn.ai.service;

import com.dawn.common.event.ClickstreamEvent;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Nghe pc-clickstream và cập nhật vector sở thích.
 * shortcut: auto-commit at-least-once, event trùng làm trọng số tăng thêm 1 (chấp nhận cho gợi ý).
 */
@Component
@Slf4j
@ConditionalOnProperty(name = "app.clickstream.consumer.enabled", havingValue = "true", matchIfMissing = true)
public class ClickstreamConsumer {

    private final UserPreferenceService preferenceService;
    private final CooccurrenceService cooccurrenceService;
    private final ObjectMapper objectMapper;
    private final String bootstrapServers;
    private final String topic;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private volatile KafkaConsumer<String, String> consumer;

    public ClickstreamConsumer(
            UserPreferenceService preferenceService,
            CooccurrenceService cooccurrenceService,
            ObjectMapper objectMapper,
            @Value("${spring.kafka.bootstrap-servers:${KAFKA_BOOTSTRAP_SERVERS:localhost:29092}}")
            String bootstrapServers,
            @Value("${app.clickstream.topic:pc-clickstream}") String topic) {
        this.preferenceService = preferenceService;
        this.cooccurrenceService = cooccurrenceService;
        this.objectMapper = objectMapper;
        this.bootstrapServers = bootstrapServers;
        this.topic = topic;
    }

    @PostConstruct
    void start() {
        Thread.ofVirtual().name("clickstream-consumer").start(this::loop);
    }

    private void loop() {
        while (running.get()) {
            try (KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<>(props())) {
                consumer = kafkaConsumer;
                kafkaConsumer.subscribe(List.of(topic));
                while (running.get()) {
                    for (ConsumerRecord<String, String> record : kafkaConsumer.poll(Duration.ofSeconds(1))) {
                        handle(record.value());
                    }
                }
            } catch (WakeupException e) {
                if (running.get()) {
                    log.warn("Clickstream consumer woken unexpectedly");
                }
            } catch (Exception e) {
                log.warn("Clickstream consumer paused: {}", e.getMessage());
                sleep();
            } finally {
                consumer = null;
            }
        }
    }

    private void handle(String payload) {
        try {
            ClickstreamEvent event = objectMapper.readValue(payload, ClickstreamEvent.class);
            preferenceService.record(event);
            cooccurrenceService.recordView(event);
        } catch (RuntimeException e) {
            // poison pill: bỏ record, không chặn partition
            log.warn("Clickstream record skipped: {}", e.getMessage());
        }
    }

    private Properties props() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "dawn-ai-clickstream");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, "dawn-ai-clickstream");
        props.put(ConsumerConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, "5000");
        props.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, "3000");
        return props;
    }

    private void sleep() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            running.set(false);
        }
    }

    @PreDestroy
    void stop() {
        running.set(false);
        KafkaConsumer<String, String> current = consumer;
        if (current != null) {
            current.wakeup();
        }
    }
}
