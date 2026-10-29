package com.dawn.common.feign;

import com.dawn.common.exception.wrapper.ConflictException;
import com.dawn.common.exception.wrapper.InvalidRequestException;
import com.dawn.common.exception.wrapper.PermissionDeniedException;
import com.dawn.common.exception.wrapper.ResourceInsufficientException;
import com.dawn.common.exception.wrapper.ResourceNotFoundException;
import com.dawn.common.exception.wrapper.UnauthorizedException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
public class CustomFeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder = new Default();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Exception decode(String methodKey, Response response) {
        String errorMessage = extractMessage(response);
        log.warn("Feign remote call [{}] returned HTTP {} - {}", methodKey, response.status(), errorMessage);

        return switch (response.status()) {
            case 400 -> new InvalidRequestException(errorMessage != null ? errorMessage : "Invalid request to remote service");
            case 401 -> new UnauthorizedException(errorMessage != null ? errorMessage : "Unauthorized remote service call");
            case 403 -> new PermissionDeniedException(errorMessage != null ? errorMessage : "Forbidden remote service call");
            case 404 -> new ResourceNotFoundException(errorMessage != null ? errorMessage : "Resource not found in remote service");
            case 409 -> new ConflictException(errorMessage != null ? errorMessage : "Conflict in remote service");
            case 422 -> new ResourceInsufficientException(errorMessage != null ? errorMessage : "Insufficient resources in remote service");
            default -> defaultDecoder.decode(methodKey, response);
        };
    }

    private String extractMessage(Response response) {
        try {
            if (response.body() != null) {
                try (InputStream inputStream = response.body().asInputStream()) {
                    String body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                    JsonNode node = objectMapper.readTree(body);
                    if (node.has("message") && !node.get("message").isNull()) {
                        return node.get("message").asText();
                    }
                    if (node.has("error") && !node.get("error").isNull()) {
                        return node.get("error").asText();
                    }
                    return body;
                }
            }
        } catch (Exception e) {
            log.debug("Failed to extract Feign error body: {}", e.getMessage());
        }
        return response.reason();
    }
}
