package com.dawn.order.dto;

import com.dawn.order.entity.enums.InvoiceStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record InvoiceDto(
        Long id,
        Long orderId,
        String invoiceNumber,
        String sellerName,
        String sellerTaxCode,
        String sellerAddress,
        String buyerName,
        String buyerTaxCode,
        String buyerAddress,
        String buyerEmail,
        BigDecimal subtotal,
        BigDecimal vatRate,
        BigDecimal vatAmount,
        BigDecimal totalAmount,
        Instant issuedAt,
        InvoiceStatus status
) {}
