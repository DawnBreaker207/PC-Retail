package com.dawn.tax.service;

import com.dawn.common.exception.wrapper.ResourceNotFoundException;
import com.dawn.tax.entity.TaxInvoice;
import com.dawn.tax.entity.enums.BuyerType;
import com.dawn.tax.entity.enums.InvoiceStatus;
import com.dawn.tax.repository.TaxInvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.Year;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaxCommandService {

    private static final BigDecimal VAT_DEFAULT = new BigDecimal("0.10");

    private final TaxInvoiceRepository taxInvoiceRepository;

    @Transactional
    public TaxInvoice generateInvoice(Long orderId) {
        return taxInvoiceRepository.findByOrderId(orderId).orElseGet(() -> {
            String invoiceNumber = "INV-" + Year.now().getValue() + "-"
                    + String.format("%06d", taxInvoiceRepository.count() + 1000);

            TaxInvoice invoice = TaxInvoice.builder()
                    .orderId(orderId)
                    .subtotal(BigDecimal.ZERO)
                    .taxAmount(BigDecimal.ZERO)
                    .total(BigDecimal.ZERO)
                    .taxRate(VAT_DEFAULT)
                    .buyerType(BuyerType.B2C)
                    .invoiceStatus(InvoiceStatus.ISSUED)
                    .invoiceNumber(invoiceNumber)
                    .issuedAt(Instant.now())
                    .build();
            return taxInvoiceRepository.save(invoice);
        });
    }

    @Transactional
    public void cancelInvoice(Long invoiceId) {
        TaxInvoice invoice = taxInvoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + invoiceId));
        invoice.setInvoiceStatus(InvoiceStatus.CANCELLED);
        taxInvoiceRepository.save(invoice);
        log.info("Invoice {} cancelled", invoiceId);
    }
}
