package com.dawn.inventory.service;

import com.dawn.common.exception.wrapper.InvalidRequestException;
import com.dawn.common.exception.wrapper.ResourceNotFoundException;
import com.dawn.common.message.Message;
import com.dawn.inventory.dto.StockDecrementCommandDto;
import com.dawn.inventory.entity.Inventory;
import com.dawn.inventory.entity.InventoryTransaction;
import com.dawn.inventory.entity.enums.ReferenceType;
import com.dawn.inventory.entity.enums.TransactionType;
import com.dawn.inventory.infra.persistence.JpaInventoryRepository;
import com.dawn.inventory.infra.persistence.JpaInventoryTransactionRepository;
import com.dawn.inventory.port.in.DeductStockUseCase;
import com.dawn.inventory.port.in.ReleaseStockUseCase;
import com.dawn.inventory.port.in.ReserveStockUseCase;
import com.dawn.inventory.port.in.RestockUseCase;
import com.dawn.inventory.port.out.InventoryCachePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryCommandService implements ReserveStockUseCase, ReleaseStockUseCase, DeductStockUseCase, RestockUseCase {

    private final JpaInventoryRepository inventoryRepository;
    private final JpaInventoryTransactionRepository transactionRepository;
    private final InventoryCachePort cachePort;

    // RESERVE
    @Override
    @Transactional
    public void reserve(List<StockDecrementCommandDto> items, Long orderId) {
        for (StockDecrementCommandDto item : items) {
            Inventory inv = inventoryRepository.findBySkuIdWithLock(item.skuId())
                    .orElseThrow(() -> new ResourceNotFoundException(Message.format(Message.Inventory.NOT_FOUND_FOR_SKU, item.skuId())));

            // Double-check after Lua gatekeeper
            if (inv.getAvailableQuantity() < item.quantity()) {
                throw new InvalidRequestException(Message.format(Message.Inventory.INSUFFICIENT_STOCK, item.skuId()));
            }

            inv.setReservedQuantity(inv.getReservedQuantity() + item.quantity());
            inventoryRepository.save(inv);

            transactionRepository.save(InventoryTransaction.builder()
                    .skuId(item.skuId())
                    .type(TransactionType.RESERVE)
                    .quantity(item.quantity())
                    .referenceType(ReferenceType.ORDER_ITEM)
                    .referenceId(orderId)
                    .build());
        }
    }

    // RELEASE
    @Override
    @Transactional
    public void release(List<StockDecrementCommandDto> items, Long orderId) {
        for (StockDecrementCommandDto item : items) {
            Inventory inv = inventoryRepository.findBySkuIdWithLock(item.skuId())
                    .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for SKU: " + item.skuId()));

            int newReserved = Math.max(0, inv.getReservedQuantity() - item.quantity());
            inv.setReservedQuantity(newReserved);
            inventoryRepository.save(inv);


            transactionRepository.save(InventoryTransaction.builder()
                    .skuId(item.skuId())
                    .type(TransactionType.RELEASE)
                    .quantity(item.quantity())
                    .referenceType(ReferenceType.ORDER_ITEM)
                    .referenceId(orderId)
                    .build());
        }

        // Update Redis after commit (AFTER_COMMIT)
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                cachePort.incrementBatch(items);
            }
        });

    }

    // DEDUCT
    @Override
    @Transactional
    public void deduct(List<StockDecrementCommandDto> items, Long orderId) {
        for (StockDecrementCommandDto item : items) {
            Inventory inv = inventoryRepository.findBySkuIdWithLock(item.skuId())
                    .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for SKU: " + item.skuId()));

            inv.setStockQuantity(inv.getStockQuantity() - item.quantity());
            inv.setReservedQuantity(Math.max(0, inv.getReservedQuantity() - item.quantity()));
            inventoryRepository.save(inv);


            transactionRepository.save(InventoryTransaction.builder()
                    .skuId(item.skuId())
                    .type(TransactionType.DEDUCT)
                    .quantity(item.quantity())
                    .referenceType(ReferenceType.ORDER_ITEM)
                    .referenceId(orderId)
                    .build());
        }
    }


    // RESTOCK
    @Override
    @Transactional
    public void restock(Long skuId, int quantity, BigDecimal unitCost, Long userId) {
        Inventory inv = inventoryRepository.findBySkuId(skuId)
                .orElseGet(() -> Inventory.builder()
                        .skuId(skuId)
                        .stockQuantity(0)
                        .reservedQuantity(0)
                        .build());

        inv.setStockQuantity(inv.getStockQuantity() + quantity);
        inventoryRepository.save(inv);

        transactionRepository.save(InventoryTransaction.builder()
                .skuId(skuId)
                .type(TransactionType.RESTOCK)
                .quantity(quantity)
                .unitCost(unitCost)
                .referenceType(ReferenceType.MANUAL)
                .createdBy(userId)
                .build());
        // Sync Redis after commit
        int newAvailable = inv.getAvailableQuantity();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                cachePort.setStock(skuId, newAvailable);
            }
        });
    }
}
