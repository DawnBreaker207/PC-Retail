package com.dawn.notification.infra.remote;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "dawn-user-contact", url = "${app.services.user-url}")
public interface UserContactFeignClient {

    @GetMapping("/internal/users/{userId}/contact")
    Map<String, String> getContact(@PathVariable("userId") Long userId);
}
