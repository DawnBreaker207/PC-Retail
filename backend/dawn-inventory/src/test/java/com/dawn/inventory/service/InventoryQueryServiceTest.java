package com.dawn.inventory.service;

import com.dawn.inventory.entity.Inventory;
import com.dawn.inventory.infra.persistence.JpaInventoryRepository;
import com.dawn.inventory.infra.persistence.JpaInventoryTransactionRepository;
import com.dawn.inventory.port.out.InventoryCachePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryQueryServiceTest {

    @Mock
    JpaInventoryRepository inventoryRepository;
    @Mock
    JpaInventoryTransactionRepository transactionRepository;
    @Mock
    InventoryCachePort cachePort;

    InventoryQueryService service;

    @BeforeEach
    void setUp() {
        service = new InventoryQueryService(inventoryRepository, transactionRepository, cachePort);
    }

    private Inventory inv(int stock, int reserved) {
        return Inventory.builder().skuId(2L).stockQuantity(stock).reservedQuantity(reserved).build();
    }

    @Test
    @DisplayName("Redis có số: dùng cache, không đụng DB")
    void cacheHit_noDb() {
        when(cachePort.getAvailable(2L)).thenReturn(7);

        assertThat(service.getAvailableQuantity(2L)).isEqualTo(7);
        verifyNoInteractions(inventoryRepository);
    }

    @Test
    @DisplayName("Redis miss: rớt DB")
    void cacheMiss_fallbackDb() {
        when(cachePort.getAvailable(2L)).thenReturn(null);
        when(inventoryRepository.findBySkuId(2L)).thenReturn(Optional.of(inv(10, 4)));

        assertThat(service.getAvailableQuantity(2L)).isEqualTo(6);
    }

    @Test
    @DisplayName("Redis chết: rớt DB thay vì nổ (graceful degradation)")
    void redisDown_fallbackDb() {
        when(cachePort.getAvailable(2L)).thenThrow(new RuntimeException("redis down"));
        when(inventoryRepository.findBySkuId(2L)).thenReturn(Optional.of(inv(10, 4)));

        assertThat(service.getAvailableQuantity(2L)).isEqualTo(6);
    }
}
