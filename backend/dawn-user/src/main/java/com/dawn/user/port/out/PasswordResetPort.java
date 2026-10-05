package com.dawn.user.port.out;

public interface PasswordResetPort {
    void storeResetToken(String tokenHash, Long userId);

    Long consumeResetToken(String tokenHash);

    /** Rate limit 1 request/60s mỗi email (docs §2.2.1). */
    boolean isOnCooldown(String email);

    void storeCooldown(String email);
}
