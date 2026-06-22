package com.dawn.user.port.out;

public interface EmailVerificationCachePort {
    void storeOtp(String email, String otp);

    String consumerOtp(String email);

    void storeCooldown(String email);

    boolean isOnCooldown(String email);
}
