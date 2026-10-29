package com.dawn.svc.gateway.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class JwtAuthFilterTest {

    private RSAPrivateKey privateKey;
    private JwtAuthFilter filter;

    @BeforeEach
    void setUp() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair pair = generator.generateKeyPair();
        privateKey = (RSAPrivateKey) pair.getPrivate();
        RSAPublicKey publicKey = (RSAPublicKey) pair.getPublic();

        ReactiveJwtDecoder decoder = NimbusReactiveJwtDecoder.withPublicKey(publicKey).build();
        ((NimbusReactiveJwtDecoder) decoder)
                .setJwtValidator(org.springframework.security.oauth2.jwt.JwtValidators.createDefaultWithIssuer("pc-retail"));
        filter = new JwtAuthFilter(decoder);
    }

    @Test
    void rejectsProtectedRouteWithoutToken() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/orders").build());

        filter.filter(exchange, chain()).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void forwardsIdentityHeadersForValidToken() throws Exception {
        AtomicReference<ServerWebExchange> seen = new AtomicReference<>();
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/orders")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token("pc-retail"))
                        // A client must not be able to spoof identity.
                        .header("X-User-Id", "999")
                        .build());

        filter.filter(exchange, capturingChain(seen)).block();

        assertEquals("42", seen.get().getRequest().getHeaders().getFirst("X-User-Id"));
        assertEquals("CUSTOMER", seen.get().getRequest().getHeaders().getFirst("X-User-Role"));
    }

    @Test
    void rejectsWrongIssuer() throws Exception {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/orders")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token("evil"))
                        .build());

        filter.filter(exchange, chain()).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void letsPublicCatalogReadThroughWithoutToken() {
        AtomicReference<ServerWebExchange> seen = new AtomicReference<>();
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/skus/123")
                        .header("X-User-Id", "999")
                        .build());

        filter.filter(exchange, capturingChain(seen)).block();

        assertNull(exchange.getResponse().getStatusCode());
        assertNull(seen.get().getRequest().getHeaders().getFirst("X-User-Id"));
    }

    @Test
    void hidesInternalEndpoints() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/internal/orders/items/exists").build());

        filter.filter(exchange, chain()).block();

        assertEquals(HttpStatus.NOT_FOUND, exchange.getResponse().getStatusCode());
    }

    private GatewayFilterChain chain() {
        return exchange -> Mono.empty();
    }

    private GatewayFilterChain capturingChain(AtomicReference<ServerWebExchange> seen) {
        return exchange -> {
            seen.set(exchange);
            return Mono.empty();
        };
    }

    private String token(String issuer) throws Exception {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(issuer)
                .subject("alice")
                .claim("id", 42L)
                .claim("role", "CUSTOMER")
                .expirationTime(Date.from(Instant.now().plusSeconds(300)))
                .build();
        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claims);
        jwt.sign(new RSASSASigner(privateKey));
        return jwt.serialize();
    }
}
