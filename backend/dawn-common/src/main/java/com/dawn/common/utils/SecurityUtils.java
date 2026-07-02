package com.dawn.common.utils;

import com.dawn.common.exception.wrapper.UnauthorizedException;
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
            throw new UnauthorizedException("Required login");
        }
        return userId;
    }
}
