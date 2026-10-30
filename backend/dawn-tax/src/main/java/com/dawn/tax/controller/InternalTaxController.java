package com.dawn.tax.controller;

import com.dawn.common.response.ResponseObject;
import com.dawn.tax.dto.TaxCalculateRequest;
import com.dawn.tax.dto.TaxCalculateResponse;
import com.dawn.tax.dto.TaxInvoiceDto;
import com.dawn.tax.service.TaxCalculationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/tax")
@RequiredArgsConstructor
public class InternalTaxController {

    private final TaxCalculationService taxCalculationService;

    @PostMapping("/calculate")
    public ResponseObject<TaxCalculateResponse> calculate(
            @Valid @RequestBody TaxCalculateRequest request) {
        return ResponseObject.success(taxCalculationService.calculateTax(request));
    }

    @GetMapping("/invoice/{orderId}")
    public ResponseObject<TaxInvoiceDto> getInvoiceByOrder(@PathVariable Long orderId) {
        return ResponseObject.success(TaxInvoiceDto.from(taxCalculationService.getByOrderId(orderId)));
    }

    @DeleteMapping("/invoice/{invoiceId}/cancel")
    public ResponseObject<Void> cancelInvoice(@PathVariable Long invoiceId) {
        taxCalculationService.cancelInvoice(invoiceId);
        return ResponseObject.deleted();
    }
}
