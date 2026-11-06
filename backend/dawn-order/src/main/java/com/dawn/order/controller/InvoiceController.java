package com.dawn.order.controller;

import com.dawn.common.security.AuthorizationConstants;
import com.dawn.common.utils.SecurityUtils;
import com.dawn.order.dto.InvoiceDto;
import com.dawn.order.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping("/orders/{orderId}/invoice")
    @PreAuthorize(AuthorizationConstants.CUSTOMER_ONLY)
    public InvoiceDto requestInvoice(@PathVariable Long orderId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return invoiceService.requestInvoice(orderId, userId);
    }

    @GetMapping("/orders/{orderId}/invoice")
    @PreAuthorize(AuthorizationConstants.CUSTOMER_ONLY)
    public InvoiceDto getInvoice(@PathVariable Long orderId) {
        return invoiceService.getByOrderId(orderId);
    }

    @GetMapping("/staff/invoices/{orderId}")
    @PreAuthorize(AuthorizationConstants.STAFF_OR_ADMIN)
    public InvoiceDto getInvoiceForStaff(@PathVariable Long orderId) {
        return invoiceService.getByOrderId(orderId);
    }
}
