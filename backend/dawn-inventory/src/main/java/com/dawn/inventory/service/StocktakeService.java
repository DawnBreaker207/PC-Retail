package com.dawn.inventory.service;

import com.dawn.common.exception.wrapper.InvalidRequestException;
import com.dawn.common.exception.wrapper.ResourceNotFoundException;
import com.dawn.inventory.controller.request.StocktakeRequest;
import com.dawn.inventory.dto.StocktakeVarianceDto;
import com.dawn.inventory.entity.Inventory;
import com.dawn.inventory.entity.Stocktake;
import com.dawn.inventory.entity.StocktakeItem;
import com.dawn.inventory.port.out.InventoryRepository;
import com.dawn.inventory.port.out.StocktakeItemRepository;
import com.dawn.inventory.port.out.StocktakeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StocktakeService {

    private final StocktakeRepository stocktakeRepo;
    private final StocktakeItemRepository itemRepo;
    private final InventoryRepository inventoryRepo;
    private final InventoryCommandService inventoryCommandService;

    @Transactional
    public Long create(StocktakeRequest req, Long staffId) {
        Stocktake session = stocktakeRepo.save(Stocktake.builder()
                .warehouseId(req.warehouseId())
                .status("DRAFT")
                .createdBy(staffId)
                .build());

        List<StocktakeItem> items = req.items().stream().map(row -> {
            Inventory inv = req.warehouseId() != null
                    ? inventoryRepo.findBySkuIdAndWarehouseIdWithLock(row.skuId(), req.warehouseId()).orElse(null)
                    : inventoryRepo.findBySkuId(row.skuId()).orElse(null);
            int systemQty = inv != null ? inv.getStockQuantity() : 0;
            return StocktakeItem.builder()
                    .stocktakeId(session.getId())
                    .skuId(row.skuId())
                    .systemQuantity(systemQty)
                    .countedQuantity(row.countedQuantity())
                    .build();
        }).toList();
        itemRepo.saveAll(items);

        return session.getId();
    }

    @Transactional
    public void complete(Long stocktakeId, Long staffId) {
        Stocktake session = stocktakeRepo.findById(stocktakeId)
                .orElseThrow(() -> new ResourceNotFoundException("Stocktake not found: " + stocktakeId));
        if (!"DRAFT".equals(session.getStatus())) {
            throw new InvalidRequestException("Stocktake " + stocktakeId + " is already " + session.getStatus());
        }

        List<StocktakeItem> items = itemRepo.findByStocktakeId(stocktakeId);
        for (StocktakeItem item : items) {
            int delta = item.getVariance();
            if (delta == 0) continue;
            Long warehouseId = session.getWarehouseId();
            if (warehouseId == null) {
                warehouseId = inventoryRepo.findBySkuId(item.getSkuId())
                        .map(Inventory::getWarehouseId).orElse(null);
            }
            if (warehouseId == null) continue;
            inventoryCommandService.adjustForStocktake(
                    item.getSkuId(), warehouseId, delta, stocktakeId, staffId);
        }

        session.setStatus("COMPLETED");
        session.setCompletedAt(Instant.now());
        stocktakeRepo.save(session);
    }

    public List<StocktakeVarianceDto> getVariances(Long stocktakeId) {
        return itemRepo.findByStocktakeId(stocktakeId).stream()
                .filter(i -> i.getVariance() != 0)
                .map(i -> new StocktakeVarianceDto(
                        i.getSkuId(), i.getSystemQuantity(), i.getCountedQuantity(), i.getVariance()))
                .toList();
    }
}
