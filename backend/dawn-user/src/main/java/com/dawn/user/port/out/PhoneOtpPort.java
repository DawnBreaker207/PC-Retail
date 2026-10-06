package com.dawn.user.port.out;

public interface PhoneOtpPort {
    void storeOtp(String phone, String otp);

    /** Đọc nhưng không xóa (verify sai nhiều lần mới đốt). */
    String peekOtp(String phone);

    void consumeOtp(String phone);

    long recordFailedAttempt(String phone);

    void clearAttempts(String phone);

    void storeCooldown(String phone);

    boolean isOnCooldown(String phone);

    void markVerified(String phone);

    boolean isVerified(String phone);

    void consumeVerified(String phone);
}
