package com.dawn.common.contract;

import com.dawn.common.contract.StockDecrementCommand;

import java.util.List;

public interface InventoryClientPort {
    List<Long> decrementBatch(List<StockDecrementCommand> items);

    void incrementBatch(List<StockDecrementCommand> items);

    void reserve(List<StockDecrementCommand> items, Long orderId);

    void release(List<StockDecrementCommand> items, Long orderId);

    void deduct(List<StockDecrementCommand> items, Long orderId);

    /**
     * Nhập kho bù khi hủy đơn ĐÃ trừ kho (từ PAID/PROCESSING).
     * Khác release (chỉ nhả reservation của PENDING_PAYMENT).
     * actorId: người thao tác (ghi created_by) — KHÔNG dùng orderId, FK users sẽ nổ.
     */
    void restock(List<StockDecrementCommand> items, Long actorId);

    /** Tìm warehouse có đủ stock cho toàn bộ cart. Trả về warehouseId. */
    Long routeWarehouse(List<StockDecrementCommand> items);
}
