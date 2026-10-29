package com.dawn.user.controller;

import com.dawn.order.port.out.GuestRiskPort;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/** API nội bộ cho order-service; bảo vệ bằng X-Internal-Token. */
@RestController
@RequestMapping("/internal/risk/guest-phone")
@RequiredArgsConstructor
public class InternalRiskController {

    private final GuestRiskPort guestRiskPort;

    @GetMapping("/verified")
    public boolean verified(@RequestParam String phone) {
        return guestRiskPort.isPhoneVerified(phone);
    }

    @PostMapping("/consume")
    public void consume(@RequestParam String phone) {
        guestRiskPort.consumePhoneVerification(phone);
    }

    @GetMapping("/abuse")
    public long abuse(@RequestParam String phone) {
        return guestRiskPort.phoneAbuseCount(phone);
    }

    @PostMapping("/abuse")
    public void recordAbuse(@RequestParam String phone) {
        guestRiskPort.recordPhoneAbuse(phone);
    }
}
