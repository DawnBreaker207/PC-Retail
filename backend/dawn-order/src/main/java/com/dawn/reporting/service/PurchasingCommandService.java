package com.dawn.reporting.service;

import com.dawn.reporting.entity.PurchaseOrder;
import com.dawn.reporting.entity.Supplier;
import com.dawn.reporting.repository.JpaPurchaseOrderRepository;
import com.dawn.reporting.repository.JpaSupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PurchasingCommandService {

    private final JpaSupplierRepository supplierRepo;
    private final JpaPurchaseOrderRepository poRepo;

    @Transactional
    public Supplier createSupplier(Supplier s) {
        s.setId(null);
        return supplierRepo.save(s);
    }

    @Transactional
    public PurchaseOrder createOrder(PurchaseOrder po) {
        po.setId(null);
        po.setStatus("DRAFT");
        return poRepo.save(po);
    }

    @Transactional
    public PurchaseOrder confirm(Long id) {
        PurchaseOrder po = poRepo.findById(id).orElseThrow();
        if (!"DRAFT".equals(po.getStatus())) return po;
        po.setStatus("CONFIRMED");
        return poRepo.save(po);
    }

    @Transactional
    public PurchaseOrder receive(Long id) {
        PurchaseOrder po = poRepo.findById(id).orElseThrow();
        if (!"CONFIRMED".equals(po.getStatus())) return po;
        po.setStatus("RECEIVED");
        return poRepo.save(po);
    }
}
