package com.dawn.user.port.out;

public interface EmailVerificationCachePort {
    void storeOtp(String email, String otp);

    String consumeOtp(String email);

    /** Đọc không xóa — chống mất OTP khi nhập sai. */
    String peekOtp(String email);

    /** Đếm lần sai, trả về tổng sau khi tăng. Quá 5 thì OTP bị đốt. */
    long recordFailedAttempt(String email);

    void clearAttempts(String email);

    void storeCooldown(String email);

    boolean isOnCooldown(String email);
}
