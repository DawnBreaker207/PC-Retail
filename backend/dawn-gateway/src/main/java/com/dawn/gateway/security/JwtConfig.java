package com.dawn.gateway.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

/**
 * Gateway verifies access tokens locally against the auth-service JWKS.
 * The private key never leaves dawn-user; key rotation is picked up automatically.
 */
@Configuration
public class JwtConfig {

    @Bean
    ReactiveJwtDecoder jwtDecoder(
            @Value("${app.jwt.jwks-uri}") String jwksUri,
            @Value("${app.jwt.issuer:pc-retail}") String issuer) {
        NimbusReactiveJwtDecoder decoder = NimbusReactiveJwtDecoder.withJwkSetUri(jwksUri).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer));
        return decoder;
    }
}
