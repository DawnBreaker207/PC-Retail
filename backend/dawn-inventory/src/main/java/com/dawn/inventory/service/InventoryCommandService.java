package com.dawn.inventory.service;

import com.dawn.common.exception.wrapper.InvalidRequestException;
import com.dawn.common.exception.wrapper.ResourceNotFoundException;
import com.dawn.common.message.Message;
import com.dawn.common.auditlog.LogConstant;
import com.dawn.common.auditlog.Loggable;
import com.dawn.common.event.InventoryDeductedEvent;
import com.dawn.common.event.InventoryLine;
import com.dawn.common.event.InventoryReleasedEvent;
import com.dawn.common.event.InventoryReservedEvent;
import com.dawn.common.outbox.OutboxPublisher;
import com.dawn.inventory.dto.StockDecrementCommandDto;
import com.dawn.inventory.entity.Inventory;
import com.dawn.inventory.entity.InventoryTransaction;
import com.dawn.inventory.entity.enums.ReferenceType;
import com.dawn.inventory.entity.enums.TransactionType;
import com.dawn.inventory.port.in.DeductStockUseCase;
import com.dawn.inventory.port.in.ReleaseStockUseCase;
import com.dawn.inventory.port.in.ReserveStockUseCase;
import com.dawn.inventory.port.in.RestockUseCase;
import com.dawn.inventory.port.out.InventoryCachePort;
import com.dawn.inventory.port.out.InventoryRepository;
import com.dawn.inventory.port.out.InventoryTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InventoryCommandService implements ReserveStockUseCase, ReleaseStockUseCase, DeductStockUseCase, RestockUseCase {

    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final InventoryCachePort cachePort;
    private final OutboxPublisher outboxPublisher;

    // RESERVE
    @Override
    @Transactional
    public void reserve(List<StockDecrementCommandDto> items, Long orderId) {
        Map<Long[], Integer> snapshot = new HashMap<>();
        for (StockDecrementCommandDto item : items) {
            Inventory inv = inventoryRepository.findBySkuIdAndWarehouseIdWithLock(item.skuId(), item.warehouseId())
                    .orElseThrow(() -> new ResourceNotFoundException(Message.format(Message.Inventory.NOT_FOUND_FOR_SKU, item.skuId())));

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

            final Long warehouseId = inv.getWarehouseId();
            final Long skuId = inv.getSkuId();
            final int available = inv.getAvailableQuantity();
            snapshot.put(new Long[]{warehouseId, skuId}, available);
        }
        outboxPublisher.publish("inventory-events", "ORDER", orderId,
                new InventoryReservedEvent(orderId, toLines(items)));
        runAfterCommit(() -> snapshot.forEach((k, v) -> cachePort.setStock(k[0], k[1], v)));
    }

    // RELEASE
    @Override
    @Transactional
    public void release(List<StockDecrementCommandDto> items, Long orderId) {
        for (StockDecrementCommandDto item : items) {
            Inventory inv = inventoryRepository.findBySkuIdAndWarehouseIdWithLock(item.skuId(), item.warehouseId())
                    .orElseThrow(() -> new ResourceNotFoundException(Message.format(Message.Inventory.NOT_FOUND_FOR_SKU, item.skuId())));

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

        outboxPublisher.publish("inventory-events", "ORDER", orderId,
                new InventoryReleasedEvent(orderId, toLines(items)));
        runAfterCommit(() -> cachePort.incrementBatch(items));
    }

    // DEDUCT
    @Override
    @Transactional
    public void deduct(List<StockDecrementCommandDto> items, Long orderId) {
        for (StockDecrementCommandDto item : items) {
            Inventory inv = inventoryRepository.findBySkuIdAndWarehouseIdWithLock(item.skuId(), item.warehouseId())
                    .orElseThrow(() -> new ResourceNotFoundException(Message.format(Message.Inventory.NOT_FOUND_FOR_SKU, item.skuId())));

            int newStock = inv.getStockQuantity() - item.quantity();
            if (newStock < 0) {
                throw new InvalidRequestException(Message.format(Message.Inventory.INSUFFICIENT_STOCK, item.skuId()));
            }
            inv.setStockQuantity(newStock);
            inv.setReservedQuantity(Math.max(0, inv.getReservedQuantity() - item.quantity()));
            inventoryRepository.save(inv);

            transactionRepository.save(InventoryTransaction.builder()
                    .skuId(item.skuId())
                    .type(TransactionType.DEDUCT)
                    .quantity(item.quantity())
                    .referenceType(ReferenceType.ORDER_ITEM)
                    .referenceId(orderId)
                    .build());

            final Long warehouseId = inv.getWarehouseId();
            final Long skuId = inv.getSkuId();
            final int available = inv.getAvailableQuantity();
            runAfterCommit(() -> cachePort.setStock(warehouseId, skuId, available));
        }
        outboxPublisher.publish("inventory-events", "ORDER", orderId,
                new InventoryDeductedEvent(orderId, toLines(items)));
    }

    // RESTOCK
    @Override
    @Transactional
    @Loggable(action = LogConstant.Action.IMPORT_STOCK, entity = LogConstant.Entity.WAREHOUSE)
    public void restock(Long skuId, int quantity, BigDecimal unitCost, Long userId) {
        if (quantity <= 0) {
            throw new InvalidRequestException(Message.format(Message.Inventory.INVALID_QUANTITY, skuId));
        }
        // Restock requires warehouseId — use this overload only for single-warehouse setups
        // For multi-warehouse, see restock(skuId, warehouseId, quantity, unitCost, userId)
        throw new UnsupportedOperationException("Use restock(skuId, warehouseId, quantity, unitCost, userId) in multi-warehouse mode");
    }

    @Transactional
    @Loggable(action = LogConstant.Action.IMPORT_STOCK, entity = LogConstant.Entity.WAREHOUSE)
    public void restock(Long skuId, Long warehouseId, int quantity, BigDecimal unitCost, Long userId) {
        if (quantity <= 0) {
            throw new InvalidRequestException(Message.format(Message.Inventory.INVALID_QUANTITY, skuId));
        }
        Inventory inv = inventoryRepository.findBySkuIdAndWarehouseIdWithLock(skuId, warehouseId)
                .orElseGet(() -> Inventory.builder()
                        .skuId(skuId)
                        .warehouseId(warehouseId)
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

        int newAvailable = inv.getAvailableQuantity();
        runAfterCommit(() -> cachePort.setStock(warehouseId, skuId, newAvailable));
    }

    private List<InventoryLine> toLines(List<StockDecrementCommandDto> items) {
        return items.stream()
                .map(item -> new InventoryLine(item.skuId(), item.quantity()))
                .toList();
    }

    /** Chạy sau commit; không có transaction thì chạy ngay (robust khi gọi lẻ/test). */
    private void runAfterCommit(Runnable task) {        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    task.run();
                }
            });
        } else {
            task.run();
        }
    }
}
