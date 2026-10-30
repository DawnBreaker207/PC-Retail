package com.dawn.media.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "app.s3")
public class S3Config {

    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucket;
    private String publicUrl;
    private String region = "us-east-1";

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .region(Region.of(region))
                .forcePathStyle(true)
                .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .region(Region.of(region))
                .build();
    }

    @PostConstruct
    public void ensureBucketExists() {
        try {
            S3Client client = s3Client();
            client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
        } catch (Exception e) {
            try {
                s3Client().createBucket(CreateBucketRequest.builder().bucket(bucket).build());
                log.info("Created S3 bucket: {}", bucket);
            } catch (Exception ex) {
                log.warn("Could not verify/create S3 bucket '{}': {}", bucket, ex.getMessage());
            }
        }
    }
}
