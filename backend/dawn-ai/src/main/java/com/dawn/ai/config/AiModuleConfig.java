package com.dawn.ai.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AiModuleConfig.AiProperties.class)
public class AiModuleConfig {

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @ConfigurationProperties(prefix = "dawn.ai")
    public record AiProperties(
            int vectorDimensions,
            Bm25Properties bm25,
            int trendingDecayHours
    ) {
        public AiProperties() {
            this(13, new Bm25Properties(1.5, 0.75), 24);
        }

        public record Bm25Properties(double k, double b) {}
    }
}
