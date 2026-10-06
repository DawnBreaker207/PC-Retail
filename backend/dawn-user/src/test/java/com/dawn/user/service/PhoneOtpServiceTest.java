package com.dawn.user.service;

import com.dawn.common.exception.wrapper.ConflictException;
import com.dawn.common.exception.wrapper.InvalidRequestException;
import com.dawn.common.redis.service.RedisService;
import com.dawn.user.port.out.PhoneOtpPort;
import com.dawn.user.port.out.SmsPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PhoneOtpServiceTest {

    @Mock
    PhoneOtpPort phoneOtpPort;
    @Mock
    SmsPort smsPort;
    @Mock
    RedisService redisService;

    PhoneOtpService service;

    @BeforeEach
    void setUp() {
        service = new PhoneOtpService(phoneOtpPort, smsPort, redisService);
        ReflectionTestUtils.setField(service, "maxPerPhone", 5L);
        ReflectionTestUtils.setField(service, "maxPerIp", 20L);
    }

    @Test
    @DisplayName("Challenge hợp lệ + chưa rate limit: gửi SMS, lưu OTP + cooldown")
    void request_ok() {
        when(redisService.get(startsWith("captcha:phone:"))).thenReturn("12");
        when(redisService.increment(startsWith("ratelimit:"), eq(1L))).thenReturn(1L);
        when(phoneOtpPort.isOnCooldown("0912345678")).thenReturn(false);

        Map<String, Object> res = service.request("0912345678", "cid", "12", "1.2.3.4");

        assertThat(res.get("sent")).isEqualTo(true);
        assertThat(res).doesNotContainKey("debugCode");
        verify(phoneOtpPort).storeOtp(eq("0912345678"), anyString());
        org.mockito.ArgumentCaptor<String> msgCap = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(smsPort).send(eq("0912345678"), msgCap.capture());
        assertThat(msgCap.getValue()).isNotBlank();
    }

    @Test
    @DisplayName("Sai captcha: 400, không gửi SMS")
    void request_badCaptcha() {
        when(redisService.get(startsWith("captcha:phone:"))).thenReturn("12");

        assertThatThrownBy(() -> service.request("0912345678", "cid", "99", "1.2.3.4"))
                .isInstanceOf(InvalidRequestException.class);

        verifyNoInteractions(smsPort);
    }

    @Test
    @DisplayName("Quá rate limit phone: 409")
    void request_rateLimited() {
        when(redisService.get(startsWith("captcha:phone:"))).thenReturn("12");
        when(redisService.increment(startsWith("ratelimit:otp:phone:"), eq(1L))).thenReturn(99L);

        assertThatThrownBy(() -> service.request("0912345678", "cid", "12", "1.2.3.4"))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("Verify đúng: true + đánh dấu verified")
    void verify_ok() {
        when(phoneOtpPort.peekOtp("0912345678")).thenReturn("123456");

        assertThat(service.verify("0912345678", "123456")).isTrue();

        verify(phoneOtpPort).consumeOtp("0912345678");
        verify(phoneOtpPort).markVerified("0912345678");
    }

    @Test
    @DisplayName("Verify sai: 400 + đếm attempt")
    void verify_wrong() {
        when(phoneOtpPort.peekOtp("0912345678")).thenReturn("123456");
        when(phoneOtpPort.recordFailedAttempt("0912345678")).thenReturn(1L);

        assertThatThrownBy(() -> service.verify("0912345678", "000000"))
                .isInstanceOf(InvalidRequestException.class);

        verify(phoneOtpPort, never()).markVerified(anyString());
    }
}
