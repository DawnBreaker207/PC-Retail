package com.dawn.payment.mock;

import com.dawn.payment.dto.PaymentDto;
import com.dawn.payment.entity.enums.PaymentMethod;
import com.dawn.payment.entity.enums.PaymentResultStatus;
import com.dawn.payment.infra.mock.MockGatewayAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class MockGatewayAdapterTest {

    MockGatewayAdapter gateway;

    @BeforeEach
    void setUp() {
        gateway = new MockGatewayAdapter();
    }

    @Test
    @DisplayName("Mặc định SUCCESS, hỗ trợ MOCK")
    void defaults() {
        assertThat(gateway.supportedMethod()).isEqualTo(PaymentMethod.MOCK);
        assertThat(gateway.scenarioOf(1L)).isEqualTo(MockGatewayAdapter.SCENARIO_SUCCESS);
    }

    @Test
    @DisplayName("Scenario lạ: 400 IllegalArgumentException")
    void unknownScenario_rejected() {
        assertThatThrownBy(() -> gateway.programScenario(1L, "BOGUS"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("SUCCESS → COMPLETED kèm mã MOCK-id")
    void success_completed() {
        PaymentDto dto = gateway.queryStatus(7L, null);

        assertThat(dto.status()).isEqualTo(PaymentResultStatus.COMPLETED);
        assertThat(dto.transactionRef()).isEqualTo("MOCK-7");
    }

    @Test
    @DisplayName("FAILED → FAILED; TIMEOUT ném lỗi để service map UNKNOWN; PENDING giữ PENDING")
    void scenarios_mapped() {
        gateway.programScenario(1L, "failed");
        assertThat(gateway.queryStatus(1L, null).status()).isEqualTo(PaymentResultStatus.FAILED);

        gateway.programScenario(2L, "TIMEOUT");
        assertThatThrownBy(() -> gateway.queryStatus(2L, null)).isInstanceOf(RuntimeException.class);

        gateway.programScenario(3L, "pending");
        assertThat(gateway.queryStatus(3L, null).status()).isEqualTo(PaymentResultStatus.PENDING);
    }

    @Test
    @DisplayName("Callback thiếu paymentId: verify fail; đủ thì program + preview")
    void callback_verifyAndProgram() {
        assertThat(gateway.verifyCallback(Map.of("result", "SUCCESS"))).isFalse();

        PaymentDto preview = gateway.processCallback(null, Map.of("paymentId", "9", "result", "FAILED"));

        assertThat(preview.id()).isEqualTo(9L);
        assertThat(gateway.scenarioOf(9L)).isEqualTo(MockGatewayAdapter.SCENARIO_FAILED);
        // webhook shares the same flow; idempotency enforced at service layer
        assertThat(gateway.processWebhook(null, Map.of("paymentId", "9")).id()).isEqualTo(9L);
    }
}
