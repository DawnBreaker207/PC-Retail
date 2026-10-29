package com.dawn.common.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/** Gọi API /internal giữa các service, kèm shared token. */
@Component
@Slf4j
public class InternalRestClient {

    private final RestTemplate restTemplate;
    private final String token;

    public InternalRestClient(RestTemplate restTemplate, @Value("${app.internal.token:}") String token) {
        this.restTemplate = restTemplate;
        this.token = token;
    }

    public <T> T exchange(String url, HttpMethod method, Object body, ParameterizedTypeReference<T> type) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Internal-Token", token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return restTemplate.exchange(url, method, new HttpEntity<>(body, headers), type).getBody();
    }
}
