package com.dawn.order.dto;

import com.dawn.order.entity.enums.WarrantyClaimStatus;

import java.time.Instant;
import java.util.List;

public record WarrantyClaimDto(
        Long id,
        Long warrantyItemId,
        String customerName,
        String customerPhone,
        String description,
        List<String> images,
        WarrantyClaimStatus status,
        String diagnosisNote,
        String resolution,
        Instant createdAt,
        Instant updatedAt
) {}
