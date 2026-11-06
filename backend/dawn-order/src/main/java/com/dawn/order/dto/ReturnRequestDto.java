package com.dawn.order.dto;

import com.dawn.order.entity.enums.ReturnStatus;

import java.time.Instant;
import java.util.List;

public record ReturnRequestDto(
        Long id,
        Long subOrderId,
        String reason,
        ReturnStatus status,
        List<String> images,
        Instant createdAt,
        Instant reviewedAt
) {}
