package com.dawn.media.service;

import com.dawn.media.config.S3Config;
import com.dawn.media.dto.MediaUploadResult;
import com.dawn.media.dto.PresignedUrlRequest;
import com.dawn.media.dto.PresignedUrlResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaService {

    private static final Duration PRESIGN_TTL = Duration.ofMinutes(15);

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final S3Config s3Config;

    public PresignedUrlResponse generatePresignedUrl(PresignedUrlRequest req) {
        String extension = extractExtension(req.filename());
        String purpose = req.purpose() != null ? req.purpose().toLowerCase() : "general";
        String objectKey = purpose + "/" + UUID.randomUUID() + extension;

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(PRESIGN_TTL)
                .putObjectRequest(r -> r
                        .bucket(s3Config.getBucket())
                        .key(objectKey)
                        .contentType(req.contentType()))
                .build();

        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(presignRequest);
        return new PresignedUrlResponse(presigned.url().toString(), objectKey, (int) PRESIGN_TTL.toSeconds());
    }

    public MediaUploadResult confirmUpload(String objectKey) {
        try {
            HeadObjectResponse head = s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(s3Config.getBucket())
                    .key(objectKey)
                    .build());
            String publicUrl = s3Config.getPublicUrl() + "/" + objectKey;
            return new MediaUploadResult(objectKey, publicUrl, head.contentType(), head.contentLength());
        } catch (NoSuchKeyException e) {
            throw new RuntimeException("Object not found: " + objectKey);
        } catch (Exception e) {
            log.error("Failed to confirm upload objectKey={}", objectKey, e);
            throw new RuntimeException("Could not verify upload: " + objectKey, e);
        }
    }

    public void deleteMedia(String objectKey) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(s3Config.getBucket())
                    .key(objectKey)
                    .build());
            log.info("Deleted object: {}", objectKey);
        } catch (Exception e) {
            log.error("Failed to delete objectKey={}", objectKey, e);
            throw new RuntimeException("Could not delete: " + objectKey, e);
        }
    }

    public boolean isHealthy() {
        try {
            s3Client.headBucket(r -> r.bucket(s3Config.getBucket()));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String extractExtension(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot) : "";
    }
}
