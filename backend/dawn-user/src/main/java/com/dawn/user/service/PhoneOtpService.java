package com.dawn.user.service;

import com.dawn.common.exception.wrapper.ConflictException;
import com.dawn.common.exception.wrapper.InvalidRequestException;
import com.dawn.common.message.Message;
import com.dawn.common.redis.service.RedisService;
import com.dawn.user.port.in.RequestPhoneOtpUseCase;
import com.dawn.user.port.in.VerifyPhoneOtpUseCase;
import com.dawn.user.port.out.PhoneOtpPort;
import com.dawn.user.port.out.SmsPort;
import com.dawn.user.util.TokenUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * OTP SĐT cho guest checkout (docs 13.5 risk-gate):
 * challenge số học + rate limit IP/phone + cooldown + sai 5 lần đốt mã.
 */
@Service
@RequiredArgsConstructor
public class PhoneOtpService implements RequestPhoneOtpUseCase, VerifyPhoneOtpUseCase {

    private static final String CAPTCHA_PREFIX = "captcha:phone:";
    private static final String RATE_PHONE_PREFIX = "ratelimit:otp:phone:";
    private static final String RATE_IP_PREFIX = "ratelimit:otp:ip:";
    private static final long MAX_ATTEMPTS = 5;

    private final PhoneOtpPort phoneOtpPort;
    private final SmsPort smsPort;
    private final RedisService redisService;

    @Value("${app.otp.debug:false}")
    private boolean otpDebug;

    @Value("${app.captcha.ttlMinutes:5}")
    private long captchaTtlMinutes;

    @Value("${app.risk.otp-per-phone-per-10m:5}")
    private long maxPerPhone;

    @Value("${app.risk.otp-per-ip-per-10m:20}")
    private long maxPerIp;

    /** Cấp challenge số học (a+b, 1..20), dùng 1 lần trong 5 phút. */
    public Map<String, String> challenge() {
        int a = ThreadLocalRandom.current().nextInt(1, 21);
        int b = ThreadLocalRandom.current().nextInt(1, 21);
        String id = UUID.randomUUID().toString();
        redisService.set(CAPTCHA_PREFIX + id, String.valueOf(a + b), Duration.ofMinutes(captchaTtlMinutes));
        return Map.of("challengeId", id, "question", a + " + " + b + " = ?");
    }

    @Override
    public Map<String, Object> request(String phone, String challengeId, String challengeAnswer, String clientIp) {
        if (!verifyAndConsumeChallenge(challengeId, challengeAnswer)) {
            throw new InvalidRequestException(Message.Auth.CAPTCHA_INVALID);
        }
        checkRateLimit(RATE_PHONE_PREFIX + phone, maxPerPhone, Message.Auth.OTP_RATE_LIMITED);
        checkRateLimit(RATE_IP_PREFIX + clientIp, maxPerIp, Message.Auth.OTP_RATE_LIMITED);
        if (phoneOtpPort.isOnCooldown(phone)) {
            throw new InvalidRequestException(Message.Auth.OTP_RESEND_COOLDOWN);
        }

        String otp = TokenUtils.generateOtp();
        phoneOtpPort.storeOtp(phone, otp);
        phoneOtpPort.storeCooldown(phone);
        smsPort.send(phone, "Ma xac thuc don hang Dawn cua ban: " + otp);

        Map<String, Object> res = new HashMap<>();
        res.put("sent", true);
        if (otpDebug) {
            res.put("debugCode", otp);
        }
        return res;
    }

    /** Đọc mã đang hiệu lực — CHỈ khi app.otp.debug=true (dev/E2E). PROD luôn 404. */
    public String debugPeek(String phone) {
        if (!otpDebug) {
            throw new InvalidRequestException(Message.Auth.TOKEN_INVALID_OR_EXPIRED);
        }
        return phoneOtpPort.peekOtp(phone);
    }

    @Override
    public boolean verify(String phone, String code) {
        String stored = phoneOtpPort.peekOtp(phone);
        if (stored == null || !stored.equals(code)) {
            long fails = phoneOtpPort.recordFailedAttempt(phone);
            if (fails >= MAX_ATTEMPTS) {
                phoneOtpPort.consumeOtp(phone);
                phoneOtpPort.clearAttempts(phone);
            }
            throw new InvalidRequestException(Message.Auth.TOKEN_INVALID_OR_EXPIRED);
        }
        phoneOtpPort.consumeOtp(phone);
        phoneOtpPort.clearAttempts(phone);
        phoneOtpPort.markVerified(phone);
        return true;
    }

    private boolean verifyAndConsumeChallenge(String challengeId, String answer) {
        if (challengeId == null || answer == null) return false;
        String key = CAPTCHA_PREFIX + challengeId;
        Object expected = redisService.get(key);
        if (expected == null) return false;
        redisService.delete(key);
        return expected.toString().trim().equals(answer.trim());
    }

    private void checkRateLimit(String key, long max, String message) {
        Long count = redisService.increment(key, 1L);
        if (count != null && count == 1L) {
            redisService.expire(key, Duration.ofMinutes(10));
        }
        if (count != null && count > max) {
            throw new ConflictException(message);
        }
    }
}
