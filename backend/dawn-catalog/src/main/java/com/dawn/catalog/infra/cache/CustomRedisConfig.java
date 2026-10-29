package com.dawn.catalog.infra.cache;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Kết nối riêng tới Redis tự build (infra/redis-custom) cho cache SKU.
 * Không đụng tới Redis chính vì server tự build chưa hỗ trợ HASH/EXPIRE/DEL.
 */
@Configuration
@ConditionalOnProperty(name = "app.cache.custom-redis.enabled", havingValue = "true")
public class CustomRedisConfig {

    @Bean
    public LettuceConnectionFactory customRedisConnectionFactory(
            @Value("${app.cache.custom-redis.host:localhost}") String host,
            @Value("${app.cache.custom-redis.port:6380}") int port) {
        return new LettuceConnectionFactory(new RedisStandaloneConfiguration(host, port));
    }

    @Bean("customRedisTemplate")
    public RedisTemplate<String, String> customRedisTemplate(LettuceConnectionFactory customRedisConnectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(customRedisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }
}
