package com.dawn.common.utils;

import com.dawn.common.security.JwtKeyProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;


@Slf4j
@Component
@RequiredArgsConstructor
public class JWTUtils {

    private final JwtKeyProvider keyProvider;

    @Value("${app.jwtExpirationsMs:900000}")
    private long jwtExpirations;

    @Value("${app.jwtRefreshExpirationsMs:604800000}")
    private long refreshTokenExpirations;

    @Value("${app.jwtCookieName:accessToken}")
    private String jwtCookie;

    @Value("${app.jwtRefreshCookieName:refreshToken}")
    private String jwtRefreshCookie;

    private final String endpoint = "/api/v1/auth/refresh-token";

    public Cookie generateJwtRefreshCookie(String refreshCookie) {
        return generateCookie(
                jwtRefreshCookie,
                refreshCookie,
                endpoint);
    }

    public Cookie getCleanJwtRefreshCookie() {
        return generateCookie(
                jwtRefreshCookie,
                "",
                endpoint);
    }

    public Long getUserIdFromToken(String token) {
        return getClaims(token).get("id", Long.class);
    }

    public String getUserNameFromToken(String token) {
        return getClaims(token).get("username", String.class);
    }

    public String getRoleFromToken(String token) {
        return getClaims(token).get("role", String.class);
    }

    private Claims getClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(keyProvider.publicKey())
                .requireIssuer(keyProvider.issuer())
                .clockSkewSeconds(60)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String generateToken(Long id, String username, String email, String role) {
        return Jwts
                .builder()
                .header().keyId(keyProvider.keyId()).and()
                .issuer(keyProvider.issuer())
                .subject(username)
                .issuedAt(new Date())
                .claim("id", id)
                .claim("username", username)
                .claim("email", email)
                .claim("role", role)
                .expiration(new Date(new Date().getTime() + jwtExpirations))
                .signWith(privateKey(), Jwts.SIG.RS256)
                .compact();
    }

    public String generateToken(String username) {
        return Jwts
                .builder()
                .header().keyId(keyProvider.keyId()).and()
                .issuer(keyProvider.issuer())
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(new Date().getTime() + jwtExpirations))
                .signWith(privateKey(), Jwts.SIG.RS256)
                .compact();
    }

    private java.security.PrivateKey privateKey() {
        if (keyProvider.privateKey() == null) {
            throw new IllegalStateException("This service has no JWT private key; token signing is auth-service only");
        }
        return keyProvider.privateKey();
    }

    public boolean validateToken(String authToken) {
        try {
            getClaims(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    private Cookie generateCookie(String name, String value, String path) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath(path);
        cookie.setMaxAge((int) Math.min(refreshTokenExpirations / 1000, Integer.MAX_VALUE));
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        return cookie;
    }
}
