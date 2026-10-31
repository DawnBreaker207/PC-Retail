package com.dawn.common.feign;

import feign.Client;
import feign.Logger;
import feign.Request;
import feign.RequestInterceptor;
import feign.Retryer;
import feign.hc5.ApacheHttp5Client;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.TimeValue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class FeignClientConfig {

    @Bean
    public RequestInterceptor internalTokenInterceptor(
            @Value("${app.internal.token:${INTERNAL_TOKEN:dawn-internal-secret}}") String configuredToken) {
        String token = (configuredToken != null && !configuredToken.isBlank())
                ? configuredToken
                : "dawn-internal-secret";
        return template -> template.header("X-Internal-Token", token);
    }

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    @Bean
    public Retryer feignRetryer() {
        // max 3 retries, backoff 100ms–1000ms
        return new Retryer.Default(100, TimeUnit.MILLISECONDS.toMillis(1000), 3);
    }

    @Bean
    public Request.Options requestOptions() {
        // connectTimeout 5s, readTimeout 10s
        return new Request.Options(5000, TimeUnit.MILLISECONDS, 10000, TimeUnit.MILLISECONDS, true);
    }

    @Bean
    public feign.codec.ErrorDecoder feignErrorDecoder() {
        return new CustomFeignErrorDecoder();
    }

    @Bean
    public Client feignClient() {
        // Apache HttpClient 5 Connection Pool: 200 total, 50 per route, keep-alive & idle eviction
        PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setMaxConnTotal(200)
                .setMaxConnPerRoute(50)
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .evictExpiredConnections()
                .evictIdleConnections(TimeValue.ofSeconds(30))
                .build();

        return new ApacheHttp5Client(httpClient);
    }
}
