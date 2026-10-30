package com.dawn.notification.dto;

import com.dawn.notification.entity.enums.NotificationChannel;

import java.util.Map;

public record NotificationEvent(
        String type,
        Long recipientId,
        String recipientEmail,
        String recipientPhone,
        NotificationChannel channel,
        String templateId,
        Map<String, Object> variables
) {}
