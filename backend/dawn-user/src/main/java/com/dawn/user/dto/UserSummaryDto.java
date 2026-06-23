package com.dawn.user.dto;

import com.dawn.user.entity.enums.SystemRole;

public record UserSummaryDto(
        Long userId,
        String fullName,
        String email,
        SystemRole systemRole) {
}
