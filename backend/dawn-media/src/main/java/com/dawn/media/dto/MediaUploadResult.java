package com.dawn.media.dto;

public record MediaUploadResult(
        String objectKey,
        String publicUrl,
        String contentType,
        Long sizeBytes
) {}
