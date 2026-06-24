package com.dawn.inventory.infra.bridge;

import com.dawn.inventory.dto.StockDecrementCommandDto;
import com.dawn.inventory.port.in.DeductStockUseCase;
import com.dawn.inventory.port.in.ReleaseStockUseCase;
import com.dawn.inventory.port.in.ReserveStockUseCase;
import com.dawn.inventory.port.out.InventoryCachePort;
import com.dawn.order.dto.StockDecrementCommand;
import com.dawn.order.port.out.InventoryClientPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class InventoryClientAdapter implements InventoryClientPort {

    private final InventoryCachePort cachePort;
    private final ReserveStockUseCase reserveStockUseCase;
    private final ReleaseStockUseCase releaseStockUseCase;
    private final DeductStockUseCase deductStockUseCase;

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

    private List<StockDecrementCommandDto> toInventoryCommands(List<StockDecrementCommand> items) {
        return items.stream()
                .map(i -> new StockDecrementCommandDto(i.skuId(), i.quantity()))
                .toList();
    }
}
