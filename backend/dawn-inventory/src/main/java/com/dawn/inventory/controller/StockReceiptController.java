package com.dawn.inventory.controller;

import com.dawn.common.security.AuthorizationConstants;
import com.dawn.common.utils.SecurityUtils;
import com.dawn.inventory.entity.StockReceipt;
import com.dawn.inventory.entity.StockReceiptItem;
import com.dawn.inventory.service.StockReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/inventory/receipts")
@RequiredArgsConstructor
@PreAuthorize(AuthorizationConstants.STAFF_OR_ADMIN)
public class StockReceiptController {

    private final StockReceiptService receiptService;

    @PostMapping
    public StockReceipt create(@RequestBody Map<String, Object> body) {
        List<StockReceiptItem> receiptItems = new java.util.ArrayList<>();
        Object raw = body.get("items");
        if (raw instanceof List<?> list) {
            for (Object o : list) {
                if (o instanceof Map<?, ?> m) {
                    StockReceiptItem item = StockReceiptItem.builder()
                            .skuId(Long.valueOf(String.valueOf(m.get("skuId"))))
                            .quantity(Integer.parseInt(String.valueOf(m.get("quantity"))))
                            .unitCost(m.get("unitCost") == null ? null
                                    : new java.math.BigDecimal(String.valueOf(m.get("unitCost"))))
                            .build();
                    receiptItems.add(item);
                }
            }
        }
        return receiptService.create(SecurityUtils.getCurrentUserId(), receiptItems);
    }

    @PostMapping("/{id}/confirm")
    public StockReceipt confirm(@PathVariable Long id) {
        return receiptService.confirm(id, SecurityUtils.getCurrentUserId());
    }
}
