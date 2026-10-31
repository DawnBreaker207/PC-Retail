package com.dawn.tax.controller;

import com.dawn.common.response.ResponseObject;
import com.dawn.common.response.ResponsePage;
import com.dawn.common.utils.SecurityUtils;
import com.dawn.tax.dto.TaxInvoiceDto;
import com.dawn.tax.service.TaxQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tax")
@RequiredArgsConstructor
public class TaxController {

    private final TaxQueryService taxQueryService;

    @GetMapping("/invoices/my")
    public ResponseObject<ResponsePage<TaxInvoiceDto>> myInvoices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = SecurityUtils.getCurrentUserId();
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        var result = taxQueryService.getByUserId(userId, pageable)
                .map(TaxInvoiceDto::from);
        return ResponseObject.success(ResponsePage.of(result));
    }

    @GetMapping("/invoices/{invoiceId}")
    public ResponseObject<TaxInvoiceDto> getInvoice(@PathVariable Long invoiceId) {
        Long userId = SecurityUtils.getCurrentUserId();
        var invoice = taxQueryService.getByUserId(userId,
                PageRequest.of(0, 1))
                .stream()
                .filter(i -> i.getId().equals(invoiceId))
                .findFirst()
                .orElseThrow(() -> new com.dawn.common.exception.wrapper.ResourceNotFoundException(
                        "Invoice not found: " + invoiceId));
        return ResponseObject.success(TaxInvoiceDto.from(invoice));
    }
}
