package com.dawn.common.feign;

import feign.Logger;
import feign.Request;
import feign.RequestInterceptor;
import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class FeignClientConfig {

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    @Bean
    public Retryer feignRetryer() {
        // Retry tối đa 3 lần, backoff từ 100ms đến 1000ms
        return new Retryer.Default(100, TimeUnit.MILLISECONDS.toMillis(1000), 3);
    }

    @Bean
    public Request.Options requestOptions() {
        // connectTimeout 5s, readTimeout 10s
        return new Request.Options(5000, TimeUnit.MILLISECONDS, 10000, TimeUnit.MILLISECONDS, true);
    }
}
