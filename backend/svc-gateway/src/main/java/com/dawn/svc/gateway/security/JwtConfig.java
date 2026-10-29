package com.dawn.svc.gateway.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Gateway verifies access tokens locally with the auth-service public key;
 * the private key never leaves auth-service.
 */
@Configuration
public class JwtConfig {

    @Bean
    ReactiveJwtDecoder jwtDecoder(
            @Value("${app.jwt.public-key}") String publicKeyValue,
            @Value("${app.jwt.issuer:pc-retail}") String issuer) throws GeneralSecurityException {
        byte[] der = Base64.getMimeDecoder().decode(publicKeyValue.strip());
        RSAPublicKey publicKey = (RSAPublicKey) KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(der));

        NimbusReactiveJwtDecoder decoder = NimbusReactiveJwtDecoder.withPublicKey(publicKey).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer));
        return decoder;
    }
}
