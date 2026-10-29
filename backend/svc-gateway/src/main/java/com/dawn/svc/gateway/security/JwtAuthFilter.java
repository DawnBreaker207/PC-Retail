package com.dawn.svc.gateway.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Edge policy: block internal/actuator paths, verify the Bearer token on
 * protected routes, and forward the identity to downstream services.
 * Services still verify the token themselves (defense in depth).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter implements GlobalFilter, Ordered {

    // Mirrors dawn-common SecurityConfig.PUBLIC_URL.
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/v1/auth/**",
            "/api/v1/brands", "/api/v1/brands/**",
            "/api/v1/categories", "/api/v1/categories/**",
            "/api/v1/skus", "/api/v1/skus/**",
            "/api/v1/builder/**",
            "/api/v1/articles", "/api/v1/articles/**",
            "/api/v1/ai/build-suggestions",
            "/api/v1/promotion/**",
            "/api/v1/cart/guest/**",
            "/api/v1/orders/guest-checkout",
            "/api/v1/payments/callback/**",
            "/api/v1/payments/webhook/**",
            "/ws/**");

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private final ReactiveJwtDecoder jwtDecoder;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (path.startsWith("/api/v1/internal/") || (path.startsWith("/actuator") && !path.startsWith("/actuator/health"))) {
            return reject(exchange, HttpStatus.NOT_FOUND, "Not found");
        }
        if (path.startsWith("/actuator/health")) {
            return chain.filter(exchange);
        }

        ServerHttpRequest.Builder request = exchange.getRequest().mutate()
                .headers(headers -> {
                    // Never trust identity headers supplied by the client.
                    headers.remove("X-User-Id");
                    headers.remove("X-User-Role");
                });

        boolean preflight = HttpMethod.OPTIONS.equals(exchange.getRequest().getMethod());
        if (preflight || isPublic(path)) {
            return chain.filter(exchange.mutate().request(request.build()).build());
        }

        String token = bearer(exchange.getRequest());
        if (token == null) {
            return reject(exchange, HttpStatus.UNAUTHORIZED, "Missing access token");
        }

        return jwtDecoder.decode(token)
                .onErrorMap(JwtException.class, InvalidTokenException::new)
                .flatMap(jwt -> {
                    Object userId = jwt.getClaim("id");
                    String role = jwt.getClaimAsString("role");
                    if (userId != null) {
                        request.header("X-User-Id", String.valueOf(userId));
                    }
                    if (role != null) {
                        request.header("X-User-Role", role);
                    }
                    return chain.filter(exchange.mutate().request(request.build()).build());
                })
                .onErrorResume(InvalidTokenException.class, e -> {
                    log.debug("Rejected access token on {}: {}", path, e.getCause().getMessage());
                    return reject(exchange, HttpStatus.UNAUTHORIZED, "Invalid or expired access token");
                });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }

    private static boolean isPublic(String path) {
        return PUBLIC_PATHS.stream().anyMatch(pattern -> PATH_MATCHER.match(pattern, path));
    }

    private static String bearer(ServerHttpRequest request) {
        String header = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        return header != null && header.startsWith("Bearer ") ? header.substring(7) : null;
    }

    private static Mono<Void> reject(ServerWebExchange exchange, HttpStatus status, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"code\":" + status.value() + ",\"message\":\"" + message + "\"}";
        return response.writeWith(Mono.just(
                response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8))));
    }

    private static final class InvalidTokenException extends RuntimeException {
        private InvalidTokenException(JwtException cause) {
            super(cause);
        }

        @Override
        public synchronized JwtException getCause() {
            return (JwtException) super.getCause();
        }
    }
}
