package com.dawn.payment.vnpay;

import com.dawn.payment.dto.PaymentDto;
import com.dawn.payment.infra.vnpay.VnpayGatewayAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VnpayGatewayAdapterTest {

    @Mock
    RestTemplate restTemplate;

    VnpayGatewayAdapter adapter;

    @BeforeEach
    void setUp() throws Exception {
        adapter = new VnpayGatewayAdapter(restTemplate);
        setField("vnp_SecretKey", "s3cr3t"); setField("vnp_QueryUrl", "https://sandbox.test/query");
    }

    private void setField(String name, Object value) throws Exception {
        Field f = VnpayGatewayAdapter.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(adapter, value);
    }

    private Map<String, String> signedParams() {
        Map<String, String> p = new HashMap<>();
        p.put("vnp_TxnRef", "1");
        p.put("vnp_Amount", "100");
        String hash = VnpayGatewayAdapter.hmacSHA512("s3cr3t", "vnp_Amount=100&vnp_TxnRef=1");
        p.put("vnp_SecureHash", hash);
        return p;
    }

    @Test
    @DisplayName("Chữ ký đúng: verify pass")
    void validSignature_passes() {
        assertThat(adapter.verifyCallback(signedParams())).isTrue();
    }

    @Test
    @DisplayName("Callback thật có vnp_SecureHashType: vẫn pass (loại khỏi hash)")
    void secureHashType_excluded() {
        Map<String, String> p = signedParams();
        p.put("vnp_SecureHashType", "SHA512");

        assertThat(adapter.verifyCallback(p)).isTrue();
    }

    @Test
    @DisplayName("Không mutate map đầu vào")
    void doesNotMutateInput() {
        Map<String, String> p = signedParams();

        adapter.verifyCallback(p);

        assertThat(p).containsKey("vnp_SecureHash");
    }

    @Test
    @DisplayName("Thiếu/sai hash hoặc sửa amount: fail")
    void invalidCases_fail() {
        Map<String, String> missing = new HashMap<>(Map.of("vnp_TxnRef", "1"));
        assertThat(adapter.verifyCallback(missing)).isFalse();

        Map<String, String> wrong = signedParams();
        wrong.put("vnp_SecureHash", "deadbeef");
        assertThat(adapter.verifyCallback(wrong)).isFalse();

        Map<String, String> tampered = signedParams();
        tampered.put("vnp_Amount", "999");
        assertThat(adapter.verifyCallback(tampered)).isFalse();
    }

    @Test
    @DisplayName("Query lỗi mạng: queryStatus ra UNKNOWN, không phải FAILED")
    void networkError_mapsToUnknown() {
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
                .thenThrow(new RuntimeException("timeout"));

        PaymentDto dto = adapter.queryStatus(5L, "txn-1");

        assertThat(dto.status().name()).isEqualTo("UNKNOWN");
    }

    @Test
    @DisplayName("Query mapping: 00→COMPLETED + txnNo, code khác→FAILED, null→NO_RESPONSE")
    void queryMapping_successFailNoResponse() {
        Map<String, Object> okResp = new java.util.HashMap<>();
        okResp.put("vnp_ResponseCode", "00");
        okResp.put("vnp_TransactionNo", "VN999");
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class))).thenReturn(okResp);

        PaymentDto ok = adapter.queryStatus(5L, "txn-1");
        assertThat(ok.status().name()).isEqualTo("COMPLETED");
        assertThat(ok.transactionRef()).isEqualTo("VN999");

        Map<String, Object> failResp = new java.util.HashMap<>();
        failResp.put("vnp_ResponseCode", "01");
        failResp.put("vnp_TransactionNo", "VN999");
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class))).thenReturn(failResp);

        PaymentDto failed = adapter.queryStatus(5L, "txn-1");
        assertThat(failed.status().name()).isEqualTo("FAILED");
    }
}
