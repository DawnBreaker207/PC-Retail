package com.dawn.reporting.service;

import com.dawn.reporting.entity.PurchaseOrder;
import com.dawn.reporting.entity.Supplier;
import com.dawn.reporting.repository.JpaPurchaseOrderRepository;
import com.dawn.reporting.repository.JpaSupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchasingQueryService {

    private final JpaSupplierRepository supplierRepo;
    private final JpaPurchaseOrderRepository poRepo;

    @Transactional(readOnly = true)
    public List<Supplier> listSuppliers() {
        return supplierRepo.findAll();
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrder> listOrders() {
        return poRepo.findAll();
    }
}
