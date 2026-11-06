package com.dawn.user.controller;

import com.dawn.user.infra.persistence.JpaUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** Internal endpoint for notification-service to resolve user contact info (email, phone). */
@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserContactController {

    private final JpaUserRepository userRepository;

    @GetMapping("/{userId}/contact")
    public Map<String, String> contact(@PathVariable Long userId) {
        return userRepository.findById(userId)
                .map(u -> Map.of(
                        "email", u.getEmail() != null ? u.getEmail() : "",
                        "phone", u.getPhone() != null ? u.getPhone() : ""))
                .orElse(Map.of("email", "", "phone", ""));
    }
}
