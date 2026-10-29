package com.dawn.common.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.AppenderBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Gửi log JSON vào Kafka để Vector đẩy sang Loki.
 * Producer tạo lazy; Kafka chết không làm chết app (AsyncAppender + neverBlock).
 */
public class KafkaLogAppender extends AppenderBase<ILoggingEvent> {

    private static final String DEFAULT_TOPIC = "app-logs";
    private static final String DEFAULT_BOOTSTRAP = "localhost:29092";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AtomicLong lastErrorAt = new AtomicLong();
    private KafkaProducer<String, String> producer;
    private String topic;
    private String service;

    @Override
    public void start() {
        topic = env("KAFKA_LOG_TOPIC", DEFAULT_TOPIC);
        service = env("SERVICE_NAME", "dawn-app");
        try {
            Properties props = new Properties();
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, env("KAFKA_BOOTSTRAP_SERVERS", DEFAULT_BOOTSTRAP));
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            props.put(ProducerConfig.ACKS_CONFIG, "1");
            props.put(ProducerConfig.RETRIES_CONFIG, "0");
            props.put(ProducerConfig.LINGER_MS_CONFIG, "100");
            props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, "1000");
            props.put(ProducerConfig.CLIENT_ID_CONFIG, service + "-logback");
            producer = new KafkaProducer<>(props);
            super.start();
        } catch (RuntimeException e) {
            report("KafkaLogAppender start failed", e);
        }
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (!isStarted() || producer == null) {
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(toPayload(event));
            producer.send(new ProducerRecord<>(topic, service, json), (metadata, error) -> {
                if (error != null) {
                    report("KafkaLogAppender send failed", error);
                }
            });
        } catch (Exception e) {
            report("KafkaLogAppender append failed", e);
        }
    }

    private Map<String, Object> toPayload(ILoggingEvent event) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("timestamp", Instant.ofEpochMilli(event.getTimeStamp()).toString());
        payload.put("level", event.getLevel().toString());
        payload.put("service", service);
        payload.put("logger", event.getLoggerName());
        payload.put("thread", event.getThreadName());
        payload.put("message", event.getFormattedMessage());

        Map<String, String> mdc = event.getMDCPropertyMap();
        if (mdc != null && !mdc.isEmpty()) {
            payload.put("traceId", mdc.get("traceId"));
            payload.put("spanId", mdc.get("spanId"));
            payload.put("mdc", mdc);
        }
        if (event.getThrowableProxy() != null) {
            payload.put("exception", ThrowableProxyUtil.asString(event.getThrowableProxy()));
        }
        return payload;
    }

    @Override
    public void stop() {
        if (producer != null) {
            try {
                producer.flush();
                producer.close(java.time.Duration.ofSeconds(2));
            } catch (RuntimeException ignored) {
                // shutdown must not throw
            }
            producer = null;
        }
        super.stop();
    }

    private void report(String message, Throwable error) {
        long now = System.currentTimeMillis();
        long last = lastErrorAt.get();
        if (now - last > 60_000 && lastErrorAt.compareAndSet(last, now)) {
            addError(message + ": " + error);
        }
    }

    private static String env(String key, String fallback) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? fallback : value;
    }
}
