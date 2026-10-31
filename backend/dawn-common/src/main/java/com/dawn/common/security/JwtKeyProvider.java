package com.dawn.common.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * RS256 key material. Only the auth service needs the private key; every other
 * service (and the gateway) only gets the public key.
 */
@Slf4j
@Component
public class JwtKeyProvider {

    private final RSAPublicKey publicKey;
    private final RSAPrivateKey privateKey;
    private final String keyId;
    private final String issuer;

    public JwtKeyProvider(
            @Value("${app.jwt.public-key:${JWT_PUBLIC_KEY:}}") String publicKeyValue,
            @Value("${app.jwt.private-key:${JWT_PRIVATE_KEY:}}") String privateKeyValue,
            @Value("${app.jwt.key-id:pc-retail}") String keyId,
            @Value("${app.jwt.issuer:pc-retail}") String issuer) {
        if (publicKeyValue == null || publicKeyValue.isBlank()) {
            throw new IllegalStateException(
                    "app.jwt.public-key is required (set JWT_PUBLIC_KEY, see infra/generate-jwt-keys.ps1)");
        }
        this.publicKey = (RSAPublicKey) decode(publicKeyValue, true);
        this.privateKey = privateKeyValue == null || privateKeyValue.isBlank()
                ? null
                : (RSAPrivateKey) decode(privateKeyValue, false);
        this.keyId = keyId;
        this.issuer = issuer;
        log.info("JWT keys loaded (kid={}, canSign={}, issuer={})", keyId, privateKey != null, issuer);
    }

    public RSAPublicKey publicKey() {
        return publicKey;
    }

    public RSAPrivateKey privateKey() {
        return privateKey;
    }

    public String keyId() {
        return keyId;
    }

    public String issuer() {
        return issuer;
    }

    /** JWKS document served at /api/v1/auth/jwks. */
    public Map<String, Object> jwks() {
        return Map.of("keys", List.of(Map.of(
                "kty", "RSA",
                "kid", keyId,
                "use", "sig",
                "alg", "RS256",
                "n", base64Url(publicKey.getModulus(), true),
                "e", base64Url(publicKey.getPublicExponent(), false))));
    }

    private static Key decode(String value, boolean publicKey) {
        try {
            byte[] der = Base64.getMimeDecoder().decode(value.strip());
            KeyFactory factory = KeyFactory.getInstance("RSA");
            return publicKey
                    ? factory.generatePublic(new X509EncodedKeySpec(der))
                    : factory.generatePrivate(new PKCS8EncodedKeySpec(der));
        } catch (IllegalArgumentException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Invalid RSA " + (publicKey ? "public" : "private") + " key", e);
        }
    }

    private static String base64Url(BigInteger value, boolean stripLeadingZero) {
        byte[] bytes = value.toByteArray();
        if (stripLeadingZero && bytes.length > 1 && bytes[0] == 0) {
            bytes = Arrays.copyOfRange(bytes, 1, bytes.length);
        }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
