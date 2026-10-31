package com.dawn.inventory.service;

import com.dawn.common.exception.wrapper.ResourceNotFoundException;
import com.dawn.common.message.Message;
import com.dawn.common.response.ResponsePage;
import com.dawn.inventory.dto.InventoryDto;
import com.dawn.inventory.dto.InventoryTransactionDto;
import com.dawn.inventory.entity.Inventory;
import com.dawn.inventory.entity.InventoryTransaction;
import com.dawn.inventory.port.in.QueryInventoryUseCase;
import com.dawn.inventory.port.out.InventoryCachePort;
import com.dawn.inventory.port.out.InventoryRepository;
import com.dawn.inventory.port.out.InventoryTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryQueryService implements QueryInventoryUseCase {

    private static final int LOW_STOCK_THRESHOLD = 5;

    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final InventoryCachePort cachePort;

    @Override
    @Transactional(readOnly = true)
    public ResponsePage<InventoryDto> getAll(int page, int size, Long skuId, boolean lowStock) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        return ResponsePage.of(inventoryRepository.findFiltered(skuId, lowStock, LOW_STOCK_THRESHOLD, pageable).map(this::toDto));
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryDto getBySkuId(Long skuId) {
        Inventory inv = inventoryRepository.findBySkuId(skuId)
                .orElseThrow(() -> new ResourceNotFoundException(Message.format(Message.Inventory.NOT_FOUND_FOR_SKU, skuId)));
        return toDto(inv);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponsePage<InventoryTransactionDto> getTransactions(Long skuId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<InventoryTransaction> result = transactionRepository.findBySkuId(skuId, pageable);
        return ResponsePage.of(result.map(this::toTransactionDto));
    }

    @Override
    public Integer getAvailableQuantity(Long skuId) {
        // Redis first; fall back to DB on failure (graceful degradation)
        try {
            Integer cached = cachePort.getAvailable(skuId);
            if (cached != null) return cached;
        } catch (RuntimeException e) {
            log.warn("Redis unavailable, fallback DB for sku {}", skuId);
        }

        // Fallback DB & warm cache
        int available = inventoryRepository.findBySkuId(skuId)
                .map(Inventory::getAvailableQuantity)
                .orElse(0);
        try {
            cachePort.syncFromDb(skuId, available);
        } catch (RuntimeException ignored) {
            // Best effort cache sync
        }
        return available;
    }


    private InventoryDto toDto(Inventory inv) {
        return new InventoryDto(
                inv.getSkuId(),
                null,
                inv.getStockQuantity(),
                inv.getReservedQuantity(),
                inv.getAvailableQuantity());
    }

    private InventoryTransactionDto toTransactionDto(InventoryTransaction t) {
        return new InventoryTransactionDto(
                t.getId(),
                t.getSkuId(),
                t.getType(),
                t.getQuantity(),
                t.getUnitCost(),
                t.getReferenceType(),
                t.getReferenceId(),
                t.getCreatedBy(),
                t.getCreatedAt()
        );
    }
}
