package com.dawn.media.dto;

public record PresignedUrlRequest(
        String filename,
        String contentType,
        String purpose
) {}
