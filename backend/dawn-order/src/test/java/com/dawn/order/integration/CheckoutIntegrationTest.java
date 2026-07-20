package com.dawn.order.integration;

import com.dawn.common.exception.wrapper.InvalidRequestException;
import com.dawn.common.redis.service.RedisService;
import com.dawn.order.controller.request.CheckoutRequest;
import com.dawn.order.dto.CartItemSnapshot;
import com.dawn.order.dto.OrderDto;
import com.dawn.order.entity.enums.FulfillmentType;
import com.dawn.order.entity.enums.PaymentStatus;
import com.dawn.order.port.out.*;
import com.dawn.order.service.CheckoutService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@SpringBootTest
@Testcontainers
public class CheckoutIntegrationTest {
    // Container
    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16")
            .withDatabaseName("dawn_test")
            .withUsername("dawn")
            .withPassword("dawn");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    static {
        // Flyway creates the schema; without it Hibernate DDL auto must do the job
        System.setProperty("spring.jpa.hibernate.ddl-auto", "update");
    }

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.properties.hibernate.jdbc.time_zone", () -> "UTC");
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    // Beans
    @Autowired
    CheckoutService checkoutService;
    @Autowired
    OrderRepository orderRepository;


    @MockitoBean
    CartClientPort cartClient;

    @MockitoBean
    CatalogClientPort catalogClient;

    @MockitoBean
    PromotionClientPort promotionClient;

    @MockitoBean
    InventoryClientPort inventoryClient;

    @MockitoBean
    RedisService redisService;

    // Constants
    private static final Long SKU_ID = 1L;
    private static final Long USER_ID = 1L;

    // Setup
    @BeforeEach
    void setup() {
        // Giả lập cart có 1 item
        given(cartClient.getItems(USER_ID))
                .willReturn(List.of(new CartItemSnapshot(SKU_ID, 2)));

        // Giả lập giá SKU
        given(catalogClient.getPrices(List.of(SKU_ID)))
                .willReturn(Map.of(SKU_ID, BigDecimal.valueOf(1_000_000)));

        // Inventory decrement thành công (list rỗng = không có SKU nào fail)
        given(inventoryClient.decrementBatch(any()))
                .willReturn(List.of());
    }

    @Test
    void checkout_happyPath_shouldCreateOrderInDB() {
        var req = buildRequest(UUID.randomUUID().toString());

        OrderDto result = checkoutService.checkout(req, USER_ID);

        assertThat(result).isNotNull();
        assertThat(result.paymentStatus()).isEqualTo(PaymentStatus.PENDING);

        // Verify DB thật
        var saved = orderRepository.findByIdempotencyKey(req.idempotencyKey());
        assertThat(saved).isPresent();
        assertThat(saved.get().getUserId()).isEqualTo(USER_ID);
    }

    @Test
    void checkout_sameIdempotencyKey_shouldReturnSameOrder() {
        String key = UUID.randomUUID().toString();

        OrderDto first = checkoutService.checkout(buildRequest(key), USER_ID);
        OrderDto second = checkoutService.checkout(buildRequest(key), USER_ID);

        assertThat(first.id()).isEqualTo(second.id());
        // Inventory chỉ bị gọi 1 lần
        verify(inventoryClient, times(1)).decrementBatch(any());
    }

    @Test
    void checkout_inventoryFail_shouldThrowAndNotCreateOrder() {
        given(inventoryClient.decrementBatch(any()))
                .willReturn(List.of(SKU_ID)); // SKU_ID fail

        var req = buildRequest(UUID.randomUUID().toString());

        assertThatThrownBy(() -> checkoutService.checkout(req, USER_ID))
                .isInstanceOf(InvalidRequestException.class);

        // Không có order trong DB
        assertThat(orderRepository.findByIdempotencyKey(req.idempotencyKey())).isEmpty();
    }

    @Disabled("SpyBean stubbing of repository.save() is complex; written/pending in integration test refactor")
    @Test
    void checkout_dbFail_shouldCompensateInventory() {
        // Giả lập DB fail sau khi inventory đã decrement
        given(orderRepository.save(any()))  // hoặc bất kỳ repo nào trong transaction
                .willThrow(new RuntimeException("DB down"));

        var req = buildRequest(UUID.randomUUID().toString());

        assertThatThrownBy(() -> checkoutService.checkout(req, USER_ID));

        // Phải gọi incrementBatch để compensate
        verify(inventoryClient, times(1)).incrementBatch(any());
    }

    // Helpers
    private CheckoutRequest buildRequest(String idempotencyKey) {
        return new CheckoutRequest(
                idempotencyKey,
                FulfillmentType.PICKUP,
                Map.of(),
                null,
                null);
    }
}
