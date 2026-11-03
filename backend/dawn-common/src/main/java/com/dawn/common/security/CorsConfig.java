package com.dawn.common.security;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Component
public class CorsConfig {
    public final List<String> ALLOWED_ORIGINS = Arrays.asList(
            "http://localhost:3000",
            "http://localhost:4000",
            "http://localhost:4200",
            "http://localhost:5173",
            "http://127.0.0.1:4000",
            "http://127.0.0.1:4200");

    private final List<String> ALLOWED_HEADERS = Arrays.asList(
            "Authorization",
            "Cache-Control",
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "Origin");

    private final List<String> ALLOWED_METHODS = Arrays.asList(
            "GET",
            "POST",
            "PUT",
            "PATCH",
            "DELETE",
            "OPTIONS");

    @Bean
    public UrlBasedCorsConfigurationSource config() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowedMethods(ALLOWED_METHODS);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
