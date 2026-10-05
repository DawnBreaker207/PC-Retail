package com.dawn.inventory.service;

import com.dawn.common.exception.wrapper.ConflictException;
import com.dawn.inventory.entity.StockReceipt;
import com.dawn.inventory.entity.StockReceiptItem;
import com.dawn.inventory.infra.persistence.JpaStockReceiptItemRepository;
import com.dawn.inventory.infra.persistence.JpaStockReceiptRepository;
import com.dawn.inventory.port.in.RestockUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockReceiptService {

    private final JpaStockReceiptRepository receiptRepo;
    private final JpaStockReceiptItemRepository itemRepo;
    private final RestockUseCase restockUseCase;

    @Transactional
    public StockReceipt create(Long staffId, List<StockReceiptItem> items) {
        if (items == null || items.isEmpty()) {
            throw new com.dawn.common.exception.wrapper.InvalidRequestException("Receipt must have at least 1 item");
        }
        for (StockReceiptItem i : items) {
            if (i.getQuantity() <= 0 || i.getSkuId() == null) {
                throw new com.dawn.common.exception.wrapper.InvalidRequestException("Receipt item invalid");
            }
        }
        StockReceipt r = receiptRepo.save(StockReceipt.builder().createdBy(staffId).status("DRAFT").build());
        for (StockReceiptItem i : items) {
            i.setId(null);
            i.setReceiptId(r.getId());
            itemRepo.save(i);
        }
        return r;
    }

    // docs §13.2: confirm idempotent — gọi lại không nhập kho 2 lần
    @Transactional
    public StockReceipt confirm(Long receiptId, Long staffId) {
        StockReceipt r = receiptRepo.findById(receiptId).orElseThrow();
        if (!"DRAFT".equals(r.getStatus())) return r;
        List<StockReceiptItem> items = itemRepo.findByReceiptId(receiptId);
        for (StockReceiptItem i : items) {
            restockUseCase.restock(i.getSkuId(), i.getQuantity(), i.getUnitCost(), staffId);
        }
        r.setStatus("CONFIRMED");
        r.setConfirmedAt(Instant.now());
        return receiptRepo.save(r);
    }
}
