package com.dawn.shipping.dto;

public record CarrierWebhookRequest(String trackingCode, String status) {}
