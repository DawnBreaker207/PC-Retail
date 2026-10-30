package com.dawn.order.integration;

import com.dawn.common.contract.CartClientPort;
import com.dawn.common.contract.CatalogClientPort;
import com.dawn.common.contract.InventoryClientPort;
import com.dawn.common.contract.PromotionClientPort;
import com.dawn.common.redis.service.RedisService;
import com.dawn.order.service.SagaSweepService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Integration test: verifies Spring context starts correctly with real Postgres, Redis, and Kafka,
 * and that SagaSweepService.doSweep() runs without error on an empty database.
 */
@SpringBootTest
@Testcontainers
class OrderSagaIT {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("pgvector/pgvector:pg17")
                    .withDatabaseName("dawn_test")
                    .withUsername("dawn")
                    .withPassword("dawn");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @Container
    static KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.6.0"));

    static {
        System.setProperty("spring.jpa.hibernate.ddl-auto", "update");
    }

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired
    SagaSweepService sagaSweepService;

    @MockitoBean CartClientPort cartClient;
    @MockitoBean CatalogClientPort catalogClient;
    @MockitoBean InventoryClientPort inventoryClient;
    @MockitoBean PromotionClientPort promotionClient;
    @MockitoBean RedisService redisService;

    @Test
    void contextLoads() {
        // Spring context started successfully with real containers
    }

    @Test
    void sagaSweep_onEmptyDb_shouldNotThrow() {
        assertThatCode(() -> sagaSweepService.doSweep()).doesNotThrowAnyException();
    }
}
