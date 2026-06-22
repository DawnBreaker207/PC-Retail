package com.dawn.user.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddressRequest(
        @NotBlank @Size(max = 255) String fullName,
        @NotBlank @Size(max = 20) String phone,
        @NotBlank @Size(max = 500) String street,
        @NotBlank @Size(max = 100) String ward,
        @NotBlank @Size(max = 100) String district,
        @NotBlank @Size(max = 100) String city,
        @JsonProperty("isDefault") boolean isDefault) {
}
