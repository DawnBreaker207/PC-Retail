package com.dawn.tax.controller;

import com.dawn.common.response.ResponseObject;
import com.dawn.tax.dto.TaxCalculateRequest;
import com.dawn.tax.dto.TaxCalculateResponse;
import com.dawn.tax.dto.TaxInvoiceDto;
import com.dawn.tax.service.TaxCommandService;
import com.dawn.tax.service.TaxQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/tax")
@RequiredArgsConstructor
public class InternalTaxController {

    private final TaxCommandService taxCommandService;
    private final TaxQueryService taxQueryService;

    @PostMapping("/calculate")
    public ResponseObject<TaxCalculateResponse> calculate(
            @Valid @RequestBody TaxCalculateRequest request) {
        return ResponseObject.success(taxQueryService.calculateTax(request));
    }

    @GetMapping("/invoice/{orderId}")
    public ResponseObject<TaxInvoiceDto> getInvoiceByOrder(@PathVariable Long orderId) {
        return ResponseObject.success(TaxInvoiceDto.from(taxQueryService.getByOrderId(orderId)));
    }

    @DeleteMapping("/invoice/{invoiceId}/cancel")
    public ResponseObject<Void> cancelInvoice(@PathVariable Long invoiceId) {
        taxCommandService.cancelInvoice(invoiceId);
        return ResponseObject.deleted();
    }
}
