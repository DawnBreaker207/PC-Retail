package com.dawn.user.infra.web;

import com.dawn.common.security.JwtKeyProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Public JWKS so gateways/services can verify access tokens offline. */
@RestController
@RequestMapping("/auth")
public class JwksController {

    private final JwtKeyProvider jwtKeyProvider;

    public JwksController(JwtKeyProvider jwtKeyProvider) {
        this.jwtKeyProvider = jwtKeyProvider;
    }

    @GetMapping("/jwks")
    public Map<String, Object> jwks() {
        return jwtKeyProvider.jwks();
    }
}
