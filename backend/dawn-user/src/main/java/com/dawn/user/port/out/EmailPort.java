package com.dawn.user.port.out;

import com.dawn.user.dto.EmailMessage;

public interface EmailPort {

    void sendWelcomeEmail(String toEmail, String recipientName);

    void sendPasswordResetEmail(String toEmail, String recipientName, String otp);

    void sendOtpVerificationEmail(String toEmail, String recipientName, String rawToken);
}
