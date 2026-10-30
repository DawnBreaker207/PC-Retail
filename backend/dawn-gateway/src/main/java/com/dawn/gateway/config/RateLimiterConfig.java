package com.dawn.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

@Configuration
public class RateLimiterConfig {

    /**
     * Resolves the rate limit key:
     * 1. If user ID is passed via header (forwarded by JwtAuthFilter), use user:ID.
     * 2. If Authorization Bearer token exists, use token snippet.
     * 3. Otherwise fallback to client IP.
     */
    @Bean
    @Primary
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            if (userId != null && !userId.isBlank()) {
                return Mono.just("user:" + userId);
            }
            String auth = exchange.getRequest().getHeaders().getFirst("Authorization");
            if (auth != null && auth.startsWith("Bearer ") && auth.length() > 15) {
                return Mono.just("token:" + auth.substring(7, Math.min(auth.length(), 25)));
            }
            InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
            String ip = (remoteAddress != null && remoteAddress.getAddress() != null)
                    ? remoteAddress.getAddress().getHostAddress()
                    : "anonymous";
            return Mono.just("ip:" + ip);
        };
    }

    /**
     * Default Redis Rate Limiter:
     * replenishRate: 100 req/sec
     * burstCapacity: 200 req
     */
    @Bean
    @Primary
    public RedisRateLimiter standardRateLimiter() {
        return new RedisRateLimiter(100, 200, 1);
    }

    /**
     * Strict Rate Limiter for checkout and auth sensitive endpoints:
     * replenishRate: 30 req/sec
     * burstCapacity: 60 req
     */
    @Bean
    public RedisRateLimiter strictRateLimiter() {
        return new RedisRateLimiter(30, 60, 1);
    }
}
