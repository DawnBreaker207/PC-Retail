package com.dawn.user.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PhoneOtpVerifyRequest(
        @NotBlank
        @Pattern(regexp = "^0\\d{9}$", message = "Invalid phone number")
        String phone,
        @NotBlank
        @Size(min = 6, max = 6)
        String code) {
}
