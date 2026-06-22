package com.dawn.user.dto;

public record EmailMessage(
        String toEmail,
        String type,
        String recipientName,
        String payload) {

    public static EmailMessage welcome(String email, String name) {
        return new EmailMessage(email, "WELCOME", name, null);
    }

    public static EmailMessage otpVerification(String email, String name, String otp) {
        return new EmailMessage(email, "OTP_VERIFICATION", name, otp);
    }

    public static EmailMessage passwordReset(String email, String name, String token) {
        return new EmailMessage(email, "PASSWORD_RESET", name, token);
    }

}
