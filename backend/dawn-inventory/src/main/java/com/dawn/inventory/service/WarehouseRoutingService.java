package com.dawn.inventory.service;

import com.dawn.common.exception.wrapper.InvalidRequestException;
import com.dawn.common.message.Message;
import com.dawn.inventory.dto.StockDecrementCommandDto;
import com.dawn.inventory.entity.Inventory;
import com.dawn.inventory.port.in.RouteWarehouseUseCase;
import com.dawn.inventory.port.out.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WarehouseRoutingService implements RouteWarehouseUseCase {

    private final InventoryRepository inventoryRepository;

    @Override
    @Transactional(readOnly = true)
    public Long routeWarehouse(List<StockDecrementCommandDto> items) {
        List<Long> skuIds = items.stream().map(StockDecrementCommandDto::skuId).toList();

        // Load all inventory for requested SKUs across all warehouses
        List<Inventory> inventories = inventoryRepository.findBySkuIdIn(skuIds);

        // Group available stock by warehouseId → skuId → availableQty
        Map<Long, Map<Long, Integer>> stockByWarehouse = inventories.stream()
                .collect(Collectors.groupingBy(
                        Inventory::getWarehouseId,
                        Collectors.toMap(Inventory::getSkuId, Inventory::getAvailableQuantity)));

        // Find warehouse that can fulfill ALL cart items; prefer the one with most total stock
        return stockByWarehouse.entrySet().stream()
                .filter(entry -> items.stream()
                        .allMatch(item -> entry.getValue().getOrDefault(item.skuId(), 0) >= item.quantity()))
                .max(Comparator.comparingInt(e ->
                        e.getValue().values().stream().mapToInt(Integer::intValue).sum()))
                .map(Map.Entry::getKey)
                .orElseThrow(() -> new InvalidRequestException(Message.Order.NO_WAREHOUSE_CAN_FULFILL));
    }
}
