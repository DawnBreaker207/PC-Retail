package com.dawn.media.dto;

public record PresignedUrlResponse(
        String uploadUrl,
        String objectKey,
        int expiresIn
) {}
