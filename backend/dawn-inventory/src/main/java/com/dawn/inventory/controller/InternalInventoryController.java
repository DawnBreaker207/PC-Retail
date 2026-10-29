package com.dawn.inventory.controller;

import com.dawn.inventory.dto.StockDecrementCommandDto;
import com.dawn.inventory.port.in.DeductStockUseCase;
import com.dawn.inventory.port.in.QueryInventoryUseCase;
import com.dawn.inventory.port.in.ReleaseStockUseCase;
import com.dawn.inventory.port.in.ReserveStockUseCase;
import com.dawn.inventory.port.in.RestockUseCase;
import com.dawn.inventory.port.out.InventoryCachePort;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** API nội bộ cho order-service; bảo vệ bằng X-Internal-Token. */
@RestController
@RequestMapping("/internal/inventory")
@RequiredArgsConstructor
public class InternalInventoryController {

    private final InventoryCachePort cachePort;
    private final ReserveStockUseCase reserveStockUseCase;
    private final ReleaseStockUseCase releaseStockUseCase;
    private final DeductStockUseCase deductStockUseCase;
    private final RestockUseCase restockUseCase;
    private final QueryInventoryUseCase queryInventoryUseCase;

    @PostMapping("/decrement-batch")
    public List<Long> decrementBatch(@RequestBody List<StockDecrementCommandDto> items) {
        return cachePort.decrementBatch(items);
    }

    @PostMapping("/increment-batch")
    public void incrementBatch(@RequestBody List<StockDecrementCommandDto> items) {
        cachePort.incrementBatch(items);
    }

    @PostMapping("/reserve")
    public void reserve(@RequestBody List<StockDecrementCommandDto> items, @RequestParam Long orderId) {
        reserveStockUseCase.reserve(items, orderId);
    }

    @PostMapping("/release")
    public void release(@RequestBody List<StockDecrementCommandDto> items, @RequestParam Long orderId) {
        releaseStockUseCase.release(items, orderId);
    }

    @PostMapping("/deduct")
    public void deduct(@RequestBody List<StockDecrementCommandDto> items, @RequestParam Long orderId) {
        deductStockUseCase.deduct(items, orderId);
    }

    @PostMapping("/restock")
    public void restock(@RequestBody List<StockDecrementCommandDto> items, @RequestParam(required = false) Long actorId) {
        for (StockDecrementCommandDto item : items) {
            restockUseCase.restock(item.skuId(), item.quantity(), null, actorId);
        }
    }

    @GetMapping("/{skuId}/available")
    public Integer available(@PathVariable Long skuId) {
        return queryInventoryUseCase.getAvailableQuantity(skuId);
    }
}
