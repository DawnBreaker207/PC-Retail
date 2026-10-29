package com.dawn.common.event;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Properties;

/**
 * Best-effort producer cho clickstream: Kafka chết không được làm hỏng request xem sản phẩm.
 * shortcut: gửi đồng bộ trong request (tối đa 500ms); khi traffic cao chuyển sang queue/async.
 */
@Component
@Slf4j
public class ClickstreamPublisher {

    private final ObjectMapper objectMapper;
    private final String bootstrapServers;
    private final String topic;
    private KafkaProducer<String, String> producer;

    public ClickstreamPublisher(
            ObjectMapper objectMapper,
            @Value("${spring.kafka.bootstrap-servers:${KAFKA_BOOTSTRAP_SERVERS:localhost:29092}}")
            String bootstrapServers,
            @Value("${app.clickstream.topic:pc-clickstream}") String topic) {
        this.objectMapper = objectMapper;
        this.bootstrapServers = bootstrapServers;
        this.topic = topic;
    }

    public void publish(ClickstreamEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            producer().send(
                    new ProducerRecord<>(topic, String.valueOf(event.userId()), payload),
                    (metadata, error) -> {
                        if (error != null) {
                            log.warn("Clickstream send failed sku={}: {}", event.skuId(), error.getMessage());
                        }
                    });
        } catch (RuntimeException e) {
            log.warn("Clickstream publish failed sku={}: {}", event.skuId(), e.getMessage());
        }
    }

    private synchronized KafkaProducer<String, String> producer() {
        if (producer == null) {
            Properties props = new Properties();
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            props.put(ProducerConfig.ACKS_CONFIG, "1");
            props.put(ProducerConfig.RETRIES_CONFIG, "0");
            props.put(ProducerConfig.LINGER_MS_CONFIG, "50");
            props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, "500");
            props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, "2000");
            props.put(ProducerConfig.CLIENT_ID_CONFIG, "dawn-clickstream");
            producer = new KafkaProducer<>(props);
        }
        return producer;
    }

    @jakarta.annotation.PreDestroy
    public synchronized void close() {
        if (producer != null) {
            producer.flush();
            producer.close(Duration.ofSeconds(2));
            producer = null;
        }
    }
}
