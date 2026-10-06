package com.dawn.user.port.in;

public interface VerifyPhoneOtpUseCase {
    /** Đúng mã → đánh dấu SĐT đã verify (TTL 30p, checkout guest dùng 1 lần). */
    boolean verify(String phone, String code);
}
