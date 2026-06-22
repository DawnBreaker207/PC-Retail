package com.dawn.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AddressDto(
        String id,
        String fullName,
        String phone,
        String street,
        String ward,
        String district,
        String city,
        @JsonProperty("isDefault") boolean isDefault) {
}
