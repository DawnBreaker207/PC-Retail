package com.dawn.common.utils;

import com.dawn.common.exception.wrapper.UnauthorizedException;
import com.dawn.common.message.Message;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public static Long getCurrentUserId() {
        Authentication auth = getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            return null;
        }
        return (Long) auth.getPrincipal();
    }

    public static String getCurrentUserRole() {
        Authentication auth = getAuthentication();
        if (auth == null || auth.getAuthorities() == null) return null;
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse(null);
    }

    public static Long requiredCurrentUserId() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new UnauthorizedException(Message.Auth.REQUIRED_LOGIN);
        }
        return userId;
    }

    /** IP client sau nginx (X-Forwarded-For) — dùng cho rate limit, không tin tuyệt đối. */
    public static String getCurrentClientIp(jakarta.servlet.http.HttpServletRequest req) {
        if (req == null) return "unknown";
        String forwarded = req.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = req.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) return realIp.trim();
        String remote = req.getRemoteAddr();
        return remote != null ? remote : "unknown";
    }
}
