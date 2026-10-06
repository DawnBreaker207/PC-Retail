package com.dawn.user.port.in;

import java.util.Map;

public interface RequestPhoneOtpUseCase {
    /**
     * @return {sent, cooldownSeconds, debugCode?} — debugCode chỉ có khi app.otp.debug=true (dev/E2E).
     */
    Map<String, Object> request(String phone, String challengeId, String challengeAnswer, String clientIp);
}
