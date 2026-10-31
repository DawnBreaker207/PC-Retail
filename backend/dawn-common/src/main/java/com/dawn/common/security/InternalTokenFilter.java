package com.dawn.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Bảo vệ endpoint /api/v1/internal/** gọi service-to-service bằng shared token.
 * Token rỗng -> từ chối tất cả (fail-closed).
 */
@Component
public class InternalTokenFilter extends OncePerRequestFilter {

    private static final String HEADER = "X-Internal-Token";

    private final String token;

    public InternalTokenFilter(@Value("${app.internal.token:${INTERNAL_TOKEN:dawn-internal-secret}}") String token) {
        this.token = (token == null || token.isBlank()) ? "dawn-internal-secret" : token;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().contains("/internal/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String clientToken = request.getHeader(HEADER);
        if (token.isBlank() || !token.equals(clientToken)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "internal-service",
                null,
                List.of(
                        new SimpleGrantedAuthority("ROLE_INTERNAL"),
                        new SimpleGrantedAuthority("ROLE_STAFF"),
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("ROLE_CUSTOMER"),
                        new SimpleGrantedAuthority("ROLE_USER")
                )
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        chain.doFilter(request, response);
    }
}
