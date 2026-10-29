package com.dawn.common.utils;

import com.dawn.common.security.JwtKeyProvider;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JWTUtilsTest {

    private JwtKeyProvider keys() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair pair = generator.generateKeyPair();
        return new JwtKeyProvider(
                Base64.getEncoder().encodeToString(pair.getPublic().getEncoded()),
                Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded()),
                "test-kid",
                "pc-retail");
    }

    private JWTUtils utils(JwtKeyProvider keys) {
        JWTUtils utils = new JWTUtils(keys);
        ReflectionTestUtils.setField(utils, "jwtExpirations", 900_000L);
        ReflectionTestUtils.setField(utils, "refreshTokenExpirations", 604_800_000L);
        return utils;
    }

    @Test
    void signsAndVerifiesRs256TokensWithIssuer() throws Exception {
        JWTUtils utils = utils(keys());

        String token = utils.generateToken(7L, "alice", "alice@example.com", "CUSTOMER");

        assertTrue(utils.validateToken(token));
        assertEquals(7L, utils.getUserIdFromToken(token));
        assertEquals("CUSTOMER", utils.getRoleFromToken(token));
        assertEquals("alice", utils.getUserNameFromToken(token));
        assertFalse(utils.validateToken(token + "tampered"));
    }

    @Test
    void exposesRsaJwksForThePublicKey() throws Exception {
        JwtKeyProvider keys = keys();

        List<?> jwksKeys = (List<?>) keys.jwks().get("keys");
        Map<?, ?> jwk = (Map<?, ?>) jwksKeys.getFirst();

        assertEquals("RSA", jwk.get("kty"));
        assertEquals("RS256", jwk.get("alg"));
        assertEquals("sig", jwk.get("use"));
        assertEquals("test-kid", jwk.get("kid"));
        assertFalse(((String) jwk.get("n")).startsWith("0"));
        assertEquals("AQAB", jwk.get("e"));
    }

    @Test
    void refusesToSignWithoutPrivateKey() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        String publicKey = Base64.getEncoder().encodeToString(generator.generateKeyPair().getPublic().getEncoded());
        JwtKeyProvider verifyOnly = new JwtKeyProvider(publicKey, "", "kid", "pc-retail");

        assertThrows(IllegalStateException.class,
                () -> utils(verifyOnly).generateToken(1L, "u", "e", "CUSTOMER"));
    }
}
