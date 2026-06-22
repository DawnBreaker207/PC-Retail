package com.dawn.user.port.out;

public interface PasswordResetPort {
    void storeResetToken(String tokenHash, Long userId);

    Long consumeResetToken(String tokenHash);
}
