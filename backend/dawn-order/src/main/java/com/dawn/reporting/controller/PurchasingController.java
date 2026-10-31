package com.dawn.reporting.controller;

import com.dawn.common.security.AuthorizationConstants;
import com.dawn.reporting.entity.PurchaseOrder;
import com.dawn.reporting.entity.Supplier;
import com.dawn.reporting.service.PurchasingCommandService;
import com.dawn.reporting.service.PurchasingQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/purchasing")
@RequiredArgsConstructor
@PreAuthorize(AuthorizationConstants.ADMIN_ONLY)
public class PurchasingController {

    private final PurchasingQueryService purchasingQueryService;
    private final PurchasingCommandService purchasingCommandService;

    @GetMapping("/suppliers")
    public List<Supplier> suppliers() {
        return purchasingQueryService.listSuppliers();
    }

    @PostMapping("/suppliers")
    public Supplier createSupplier(@RequestBody Supplier s) {
        return purchasingCommandService.createSupplier(s);
    }

    @GetMapping("/orders")
    public List<PurchaseOrder> orders() {
        return purchasingQueryService.listOrders();
    }

    @PostMapping("/orders")
    public PurchaseOrder createOrder(@RequestBody PurchaseOrder po) {
        return purchasingCommandService.createOrder(po);
    }

    @PostMapping("/orders/{id}/confirm")
    public PurchaseOrder confirm(@PathVariable Long id) {
        return purchasingCommandService.confirm(id);
    }

    @PostMapping("/orders/{id}/receive")
    public PurchaseOrder receive(@PathVariable Long id) {
        return purchasingCommandService.receive(id);
    }
}
