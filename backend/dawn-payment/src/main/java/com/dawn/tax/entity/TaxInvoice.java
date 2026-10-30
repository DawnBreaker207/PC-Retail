package com.dawn.tax.entity;

import com.dawn.tax.entity.enums.BuyerType;
import com.dawn.tax.entity.enums.InvoiceStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "tax_invoices")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false, unique = true)
    private Long orderId;

    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "buyer_type", nullable = false, length = 10)
    private BuyerType buyerType;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "tax_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal taxAmount;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal total;

    @Column(name = "tax_rate", nullable = false, precision = 5, scale = 4)
    private BigDecimal taxRate;

    @Column(name = "invoice_number", unique = true, length = 50)
    private String invoiceNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "invoice_status", nullable = false, length = 20)
    private InvoiceStatus invoiceStatus;

    @Column(name = "business_tax_code", length = 20)
    private String businessTaxCode;

    @Column(name = "issued_at")
    private Instant issuedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
        if (invoiceStatus == null) {
            invoiceStatus = InvoiceStatus.DRAFT;
        }
        if (buyerType == null) {
            buyerType = BuyerType.B2C;
        }
    }
}
