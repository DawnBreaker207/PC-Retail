package com.dawn.common.websocket;

import com.dawn.common.utils.JWTUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Authenticates the STOMP CONNECT frame using the native Authorization
 * header (SockJS polling transports cannot send HTTP headers, so the
 * handshake itself stays public and auth is enforced here instead).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final JWTUtils jwtUtils;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            List<String> authHeaders = accessor.getNativeHeader("Authorization");
            String header = (authHeaders == null || authHeaders.isEmpty()) ? null : authHeaders.get(0);
            if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
                String jwt = header.substring(7);
                try {
                    if (jwtUtils.validateToken(jwt)) {
                        Long userId = jwtUtils.getUserIdFromToken(jwt);
                        String role = jwtUtils.getRoleFromToken(jwt);
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        userId,
                                        null,
                                        List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role)));
                        accessor.setUser(authentication);
                        return message;
                    }
                } catch (Exception e) {
                    log.warn("Rejecting STOMP CONNECT: {}", e.getMessage());
                }
            }
            log.warn("Rejecting STOMP CONNECT without valid Bearer token");
            throw new org.springframework.messaging.MessagingException("Missing or invalid Authorization header");
        }
        return message;
    }
}
