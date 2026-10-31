package com.dawn.inventory.infra.bridge;

import com.dawn.common.contract.InventoryQueryPort;
import com.dawn.inventory.dto.StockDecrementCommandDto;
import com.dawn.inventory.port.in.DeductStockUseCase;
import com.dawn.inventory.port.in.QueryInventoryUseCase;
import com.dawn.inventory.port.in.ReleaseStockUseCase;
import com.dawn.inventory.port.in.ReserveStockUseCase;
import com.dawn.inventory.port.in.RestockUseCase;
import com.dawn.inventory.port.out.InventoryCachePort;
import com.dawn.common.contract.StockDecrementCommand;
import com.dawn.common.contract.InventoryClientPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class InventoryClientAdapter implements InventoryClientPort, InventoryQueryPort {

    private final InventoryCachePort cachePort;
    private final ReserveStockUseCase reserveStockUseCase;
    private final ReleaseStockUseCase releaseStockUseCase;
    private final DeductStockUseCase deductStockUseCase;
    private final RestockUseCase restockUseCase;
    private final QueryInventoryUseCase queryInventoryUseCase;

    @Override
    public List<Long> decrementBatch(List<StockDecrementCommand> items) {
        return cachePort.decrementBatch(toInventoryCommands(items));
    }

    @Override
    public void incrementBatch(List<StockDecrementCommand> items) {
        cachePort.incrementBatch(toInventoryCommands(items));
    }

    @Override
    public void reserve(List<StockDecrementCommand> items, Long orderId) {
        reserveStockUseCase.reserve(toInventoryCommands(items), orderId);
    }

    @Override
    public void release(List<StockDecrementCommand> items, Long orderId) {
        releaseStockUseCase.release(toInventoryCommands(items), orderId);
    }

    @Override
    public void deduct(List<StockDecrementCommand> items, Long orderId) {
        deductStockUseCase.deduct(toInventoryCommands(items), orderId);
    }

    @Override
    public void restock(List<StockDecrementCommand> items, Long actorId) {
        for (StockDecrementCommandDto cmd : toInventoryCommands(items)) {
            // actorId nullable: null means system-initiated restock; see SubOrder.statusHistory
            restockUseCase.restock(cmd.skuId(), cmd.quantity(), null, actorId);
        }
    }

    @Override
    public Integer getAvailableQuantity(Long skuId) {
        return queryInventoryUseCase.getAvailableQuantity(skuId);
    }

    private List<StockDecrementCommandDto> toInventoryCommands(List<StockDecrementCommand> items) {
        return items.stream()
                .map(i -> new StockDecrementCommandDto(i.skuId(), i.quantity()))
                .toList();
    }
}
