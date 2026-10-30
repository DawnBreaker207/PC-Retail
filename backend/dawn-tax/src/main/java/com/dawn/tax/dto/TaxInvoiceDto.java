package com.dawn.tax.dto;

import com.dawn.tax.entity.TaxInvoice;
import com.dawn.tax.entity.enums.BuyerType;
import com.dawn.tax.entity.enums.InvoiceStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record TaxInvoiceDto(
        Long id,
        Long orderId,
        Long userId,
        BuyerType buyerType,
        BigDecimal subtotal,
        BigDecimal taxAmount,
        BigDecimal total,
        BigDecimal taxRate,
        String invoiceNumber,
        InvoiceStatus invoiceStatus,
        String businessTaxCode,
        Instant issuedAt,
        Instant createdAt
) {
    public static TaxInvoiceDto from(TaxInvoice inv) {
        return new TaxInvoiceDto(
                inv.getId(), inv.getOrderId(), inv.getUserId(),
                inv.getBuyerType(), inv.getSubtotal(), inv.getTaxAmount(),
                inv.getTotal(), inv.getTaxRate(), inv.getInvoiceNumber(),
                inv.getInvoiceStatus(), inv.getBusinessTaxCode(),
                inv.getIssuedAt(), inv.getCreatedAt()
        );
    }
}
