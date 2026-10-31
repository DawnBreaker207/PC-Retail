package com.dawn.media.service;

import com.dawn.media.config.S3Config;
import com.dawn.media.dto.MediaUploadResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaCommandService {

    private final S3Client s3Client;
    private final S3Config s3Config;

    @Transactional
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

    @Transactional
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
}
