package com.dawn.promotion.infra.persistence;

import com.dawn.promotion.entity.enums.DiscountType;
import com.dawn.promotion.entity.enums.ScopeType;
import com.dawn.promotion.entity.Voucher;
import com.dawn.promotion.entity.VoucherUsage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration thật với Postgres (cần Docker): lưới UNIQUE(voucher_id, user_id)
 * phải chặn dùng voucher 2 lần dù service check-then-insert bị đua.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EntityScan("com.dawn.promotion.entity")
@EnableJpaRepositories("com.dawn.promotion.infra.persistence")
@Testcontainers
class VoucherUsageUniqueIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer("postgres:16")
            .withDatabaseName("dawn_voucher_test")
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
    JpaVoucherRepository voucherRepo;
    @Autowired
    JpaVoucherUsageRepository usageRepo;

    @Test
    @DisplayName("Cùng (voucher, user) lưu 2 lần: lần 2 nổ DataIntegrityViolation")
    void duplicateUsage_rejectedByConstraint() {
        Voucher v = voucherRepo.saveAndFlush(Voucher.builder()
                .code("TEST10")
                .scopeType(ScopeType.STOREWIDE)
                .discountType(DiscountType.PERCENT)
                .discountValue(BigDecimal.TEN)
                .minOrderValue(BigDecimal.ZERO)
                .totalQuantity(100)
                .remainingQuantity(100)
                .validFrom(Instant.now().minusSeconds(60))
                .validTo(Instant.now().plusSeconds(3600))
                .build());

        usageRepo.saveAndFlush(VoucherUsage.builder()
                .voucherId(v.getId()).userId(7L).orderId(1L)
                .discountAmount(BigDecimal.TEN).build());

        assertThatThrownBy(() -> usageRepo.saveAndFlush(VoucherUsage.builder()
                .voucherId(v.getId()).userId(7L).orderId(2L)
                .discountAmount(BigDecimal.TEN).build()))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }
}
