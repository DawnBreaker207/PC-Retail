package com.dawn.order.infra.persistence;

import com.dawn.order.entity.SubOrder;
import com.dawn.order.entity.enums.FulfillmentType;
import com.dawn.order.entity.enums.SubOrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration thật với Postgres (cần Docker): cổng CAS phải atomic ở DB.
 * Không chạy được ở máy không có Docker — CI chạy.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EntityScan("com.dawn.order.entity")
@EnableJpaRepositories("com.dawn.order.infra.persistence")
@Testcontainers
class SubOrderCasRepositoryIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer("postgres:16")
            .withDatabaseName("dawn_cas_test")
            .withUsername("dawn")
            .withPassword("dawn");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.flyway.enabled", () -> "false");
    }

    @Autowired
    JpaSubOrderRepository repo;

    private SubOrder pending() {
        SubOrder so = SubOrder.builder()
                .orderId(1L)
                .fulfillmentType(FulfillmentType.DELIVERY)
                .subtotal(BigDecimal.TEN)
                .finalAmount(BigDecimal.TEN)
                .status(SubOrderStatus.PENDING_PAYMENT)
                .statusHistory(new ArrayList<>(List.of()))
                .build();
        return repo.saveAndFlush(so);
    }

    @Test
    @DisplayName("CAS thắng 1 lần, lần 2 với expected cũ trả 0 row")
    void cas_firstWinsSecondLoses() {
        SubOrder so = pending();

        int first = repo.transitionIfStatus(so.getId(), SubOrderStatus.PENDING_PAYMENT, SubOrderStatus.PAID);
        int second = repo.transitionIfStatus(so.getId(), SubOrderStatus.PENDING_PAYMENT, SubOrderStatus.PAID);

        assertThat(first).isOne();
        assertThat(second).isZero();
        assertThat(((org.springframework.data.jpa.repository.JpaRepository<SubOrder, Long>) repo)
                .findById(so.getId())).map(SubOrder::getStatus)
                .hasValue(SubOrderStatus.PAID);
    }

    @Test
    @DisplayName("CAS với expected đúng hiện tại thì đi tiếp được")
    void cas_correctExpected_movesForward() {
        SubOrder so = pending();

        repo.transitionIfStatus(so.getId(), SubOrderStatus.PENDING_PAYMENT, SubOrderStatus.PAID);
        int next = repo.transitionIfStatus(so.getId(), SubOrderStatus.PAID, SubOrderStatus.PROCESSING);

        assertThat(next).isOne();
    }
}
