package com.dawn.user.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PhoneOtpRequest(
        @NotBlank
        @Pattern(regexp = "^0\\d{9}$", message = "Invalid phone number")
        String phone,
        @NotBlank String challengeId,
        @NotBlank String challengeAnswer) {
}
