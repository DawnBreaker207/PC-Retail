package com.dawn.payment.dto;

public record VnpayQueryResult(
        boolean success,
        String transactionNo,
        String responseCode) {
}
