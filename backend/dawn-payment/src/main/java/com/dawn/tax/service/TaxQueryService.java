package com.dawn.tax.service;

import com.dawn.common.exception.wrapper.ResourceNotFoundException;
import com.dawn.tax.dto.TaxCalculateRequest;
import com.dawn.tax.dto.TaxCalculateResponse;
import com.dawn.tax.dto.TaxLineItem;
import com.dawn.tax.entity.TaxInvoice;
import com.dawn.tax.entity.enums.BuyerType;
import com.dawn.tax.repository.TaxInvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TaxQueryService {

    private static final BigDecimal VAT_COMPONENT = new BigDecimal("0.10");
    private static final BigDecimal VAT_ACCESSORY = new BigDecimal("0.08");
    private static final BigDecimal VAT_DEFAULT = new BigDecimal("0.10");
    private static final BigDecimal VAT_EXEMPT = BigDecimal.ZERO;

    private final TaxInvoiceRepository taxInvoiceRepository;

    @Transactional(readOnly = true)
    public TaxCalculateResponse calculateTax(TaxCalculateRequest req) {
        if (req.buyerType() == BuyerType.B2B
                && req.businessTaxCode() != null
                && !req.businessTaxCode().isBlank()) {
            return buildExemptResponse(req.items());
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal taxTotal = BigDecimal.ZERO;
        Map<String, BigDecimal> breakdown = new HashMap<>();

        for (TaxLineItem item : req.items()) {
            BigDecimal lineSubtotal = item.unitPrice()
                    .multiply(BigDecimal.valueOf(item.quantity()));
            BigDecimal rate = rateFor(item.productType());
            BigDecimal lineTax = lineSubtotal.multiply(rate).setScale(2, RoundingMode.HALF_UP);

            subtotal = subtotal.add(lineSubtotal);
            taxTotal = taxTotal.add(lineTax);
            breakdown.merge(item.productType(), lineTax, BigDecimal::add);
        }

        BigDecimal total = subtotal.add(taxTotal);
        BigDecimal effectiveRate = subtotal.compareTo(BigDecimal.ZERO) == 0
                ? VAT_DEFAULT
                : taxTotal.divide(subtotal, 4, RoundingMode.HALF_UP);

        return new TaxCalculateResponse(subtotal, taxTotal, total, effectiveRate, breakdown);
    }

    @Transactional(readOnly = true)
    public TaxInvoice getByOrderId(Long orderId) {
        return taxInvoiceRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found for order: " + orderId));
    }

    @Transactional(readOnly = true)
    public Page<TaxInvoice> getByUserId(Long userId, Pageable pageable) {
        return taxInvoiceRepository.findByUserId(userId, pageable);
    }

    private BigDecimal rateFor(String productType) {
        if (productType == null) return VAT_DEFAULT;
        return switch (productType.toUpperCase()) {
            case "COMPONENT" -> VAT_COMPONENT;
            case "ACCESSORY" -> VAT_ACCESSORY;
            default -> VAT_DEFAULT;
        };
    }

    private TaxCalculateResponse buildExemptResponse(List<TaxLineItem> items) {
        BigDecimal subtotal = items.stream()
                .map(i -> i.unitPrice().multiply(BigDecimal.valueOf(i.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new TaxCalculateResponse(subtotal, BigDecimal.ZERO, subtotal, VAT_EXEMPT, Map.of());
    }
}
