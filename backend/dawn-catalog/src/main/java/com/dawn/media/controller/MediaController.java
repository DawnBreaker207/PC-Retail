package com.dawn.media.controller;

import com.dawn.common.response.ResponseObject;
import com.dawn.common.security.AuthorizationConstants;
import com.dawn.media.dto.MediaUploadResult;
import com.dawn.media.dto.PresignedUrlRequest;
import com.dawn.media.dto.PresignedUrlResponse;
import com.dawn.media.service.MediaCommandService;
import com.dawn.media.service.MediaQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaCommandService mediaCommandService;
    private final MediaQueryService mediaQueryService;

    @PostMapping("/presigned")
    public ResponseObject<PresignedUrlResponse> generatePresignedUrl(
            @RequestBody PresignedUrlRequest request) {
        return ResponseObject.success(mediaQueryService.generatePresignedUrl(request));
    }

    @PostMapping("/confirm")
    public ResponseObject<MediaUploadResult> confirmUpload(
            @RequestBody Map<String, String> body) {
        String objectKey = body.get("objectKey");
        return ResponseObject.success(mediaCommandService.confirmUpload(objectKey));
    }

    @DeleteMapping("/{objectKey}")
    @PreAuthorize(AuthorizationConstants.ADMIN_ONLY)
    public ResponseObject<Void> deleteMedia(@PathVariable String objectKey) {
        mediaCommandService.deleteMedia(objectKey);
        return ResponseObject.deleted();
    }

    @GetMapping("/health")
    public ResponseObject<Map<String, Boolean>> healthCheck() {
        return ResponseObject.success(Map.of("minioReachable", mediaQueryService.isHealthy()));
    }
}
