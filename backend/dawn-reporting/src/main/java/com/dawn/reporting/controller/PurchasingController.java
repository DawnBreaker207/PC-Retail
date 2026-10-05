package com.dawn.reporting.controller;

import com.dawn.common.security.AuthorizationConstants;
import com.dawn.reporting.entity.PurchaseOrder;
import com.dawn.reporting.entity.Supplier;
import com.dawn.reporting.infra.persistence.JpaPurchaseOrderRepository;
import com.dawn.reporting.infra.persistence.JpaSupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/purchasing")
@RequiredArgsConstructor
@PreAuthorize(AuthorizationConstants.ADMIN_ONLY)
public class PurchasingController {

    private final JpaSupplierRepository supplierRepo;
    private final JpaPurchaseOrderRepository poRepo;

    @GetMapping("/suppliers")
    public List<Supplier> suppliers() {
        return supplierRepo.findAll();
    }

    @PostMapping("/suppliers")
    public Supplier createSupplier(@RequestBody Supplier s) {
        s.setId(null);
        return supplierRepo.save(s);
    }

    @GetMapping("/orders")
    public List<PurchaseOrder> orders() {
        return poRepo.findAll();
    }

    @PostMapping("/orders")
    public PurchaseOrder createOrder(@RequestBody PurchaseOrder po) {
        po.setId(null);
        po.setStatus("DRAFT");
        return poRepo.save(po);
    }

    @PostMapping("/orders/{id}/confirm")
    public PurchaseOrder confirm(@PathVariable Long id) {
        PurchaseOrder po = poRepo.findById(id).orElseThrow();
        if (!"DRAFT".equals(po.getStatus())) return po;
        po.setStatus("CONFIRMED");
        return poRepo.save(po);
    }

    @PostMapping("/orders/{id}/receive")
    public PurchaseOrder receive(@PathVariable Long id) {
        PurchaseOrder po = poRepo.findById(id).orElseThrow();
        if (!"CONFIRMED".equals(po.getStatus())) return po;
        po.setStatus("RECEIVED");
        return poRepo.save(po);
    }
}
