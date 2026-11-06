package com.dawn.inventory.port.in;

import com.dawn.inventory.dto.StockDecrementCommandDto;

import java.util.List;

public interface RouteWarehouseUseCase {
    /** Trả về warehouseId có thể fulfill toàn bộ cart. Throws nếu không có warehouse nào đủ hàng. */
    Long routeWarehouse(List<StockDecrementCommandDto> items);
}
