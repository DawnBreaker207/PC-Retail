package com.dawn.inventory.service;

import com.dawn.common.exception.wrapper.InvalidRequestException;
import com.dawn.common.outbox.OutboxPublisher;
import com.dawn.inventory.dto.StockDecrementCommandDto;
import com.dawn.inventory.entity.Inventory;
import com.dawn.inventory.port.out.InventoryCachePort;
import com.dawn.inventory.port.out.InventoryRepository;
import com.dawn.inventory.port.out.InventoryTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryCommandServiceTest {

    @Mock
    InventoryRepository inventoryRepository;
    @Mock
    InventoryTransactionRepository transactionRepository;
    @Mock
    InventoryCachePort cachePort;
    @Mock
    OutboxPublisher outboxPublisher;

    InventoryCommandService service;

    @BeforeEach
    void setUp() {
        service = new InventoryCommandService(inventoryRepository, transactionRepository, cachePort, outboxPublisher);
    }

    private static final Long WH = 1L;

    private Inventory inv(int stock, int reserved) {
        return Inventory.builder().skuId(1L).warehouseId(WH).stockQuantity(stock).reservedQuantity(reserved).build();
    }

    private StockDecrementCommandDto cmd(int qty) {
        return new StockDecrementCommandDto(1L, qty, WH);
    }

    @Test
    @DisplayName("Reserve: Redis snapshot = available sau trừ, không trừ 2 lần")
    void reserve_snapshotIsAvailable() {
        when(inventoryRepository.findBySkuIdAndWarehouseIdWithLock(1L, WH)).thenReturn(Optional.of(inv(10, 2)));

        service.reserve(List.of(cmd(3)), 99L);

        // available = 10 - (2+3) = 5
        verify(cachePort).setStock(WH, 1L, 5);
    }

    @Test
    @DisplayName("Reserve quá tồn: throw, không đụng Redis")
    void reserve_insufficient_throws() {
        when(inventoryRepository.findBySkuIdAndWarehouseIdWithLock(1L, WH)).thenReturn(Optional.of(inv(2, 0)));

        assertThatThrownBy(() -> service.reserve(List.of(cmd(5)), 99L))
                .isInstanceOf(InvalidRequestException.class);

        verifyNoInteractions(cachePort);
    }

    @Test
    @DisplayName("Restock số lượng <= 0: throw")
    void restock_nonPositive_throws() {
        assertThatThrownBy(() -> service.restock(1L, 0, BigDecimal.ONE, 7L))
                .isInstanceOf(InvalidRequestException.class);
        assertThatThrownBy(() -> service.restock(1L, -5, BigDecimal.ONE, 7L))
                .isInstanceOf(InvalidRequestException.class);

        verifyNoInteractions(inventoryRepository);
    }

    @Test
    @DisplayName("Deduct vượt tồn: throw, không đụng Redis")
    void deduct_exceeding_throws() {
        when(inventoryRepository.findBySkuIdAndWarehouseIdWithLock(1L, WH)).thenReturn(Optional.of(inv(2, 2)));

        assertThatThrownBy(() -> service.deduct(List.of(cmd(5)), 99L))
                .isInstanceOf(InvalidRequestException.class);

        verifyNoInteractions(cachePort);
    }

    @Test
    @DisplayName("Release vượt reserved: kẹp về 0, không âm")
    void release_clampsAtZero() {
        Inventory entity = inv(10, 1);
        when(inventoryRepository.findBySkuIdAndWarehouseIdWithLock(1L, WH)).thenReturn(Optional.of(entity));

        service.release(List.of(cmd(5)), 99L);

        assertThat(entity.getReservedQuantity()).isZero();
    }
}
