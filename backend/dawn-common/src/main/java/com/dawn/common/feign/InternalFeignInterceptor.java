package com.dawn.common.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * OpenFeign RequestInterceptor:
 * 1. Đính kèm X-Internal-Token để vượt qua bộ lọc bảo vệ /internal/** của service đích.
 * 2. Tự động forward danh tính (X-User-Id, X-User-Role) nếu đang trong luồng xử lý web request.
 */
@Component
public class InternalFeignInterceptor implements RequestInterceptor {

    private final String internalToken;

    public InternalFeignInterceptor(@Value("${app.internal.token:dev-internal-token}") String internalToken) {
        this.internalToken = internalToken;
    }

    @Override
    public void apply(RequestTemplate template) {
        // Luôn gửi shared internal token
        template.header("X-Internal-Token", internalToken);

        // Forward identity headers nếu có HTTP request hiện tại
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String userId = request.getHeader("X-User-Id");
            String userRole = request.getHeader("X-User-Role");
            if (userId != null && !userId.isBlank()) {
                template.header("X-User-Id", userId);
            }
            if (userRole != null && !userRole.isBlank()) {
                template.header("X-User-Role", userRole);
            }
        }
    }
}
