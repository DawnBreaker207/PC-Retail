package com.dawn.inventory.infra.bridge;

import com.dawn.inventory.dto.StockDecrementCommandDto;
import com.dawn.inventory.port.in.DeductStockUseCase;
import com.dawn.inventory.port.in.QueryInventoryUseCase;
import com.dawn.inventory.port.in.ReleaseStockUseCase;
import com.dawn.inventory.port.in.ReserveStockUseCase;
import com.dawn.inventory.port.in.RestockUseCase;
import com.dawn.inventory.port.out.InventoryCachePort;
import com.dawn.order.dto.StockDecrementCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryClientAdapterTest {

    @Mock
    InventoryCachePort cachePort;
    @Mock
    ReserveStockUseCase reserveStockUseCase;
    @Mock
    ReleaseStockUseCase releaseStockUseCase;
    @Mock
    DeductStockUseCase deductStockUseCase;
    @Mock
    RestockUseCase restockUseCase;
    @Mock
    QueryInventoryUseCase queryInventoryUseCase;

    InventoryClientAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new InventoryClientAdapter(
                cachePort, reserveStockUseCase, releaseStockUseCase,
                deductStockUseCase, restockUseCase, queryInventoryUseCase);
    }

    @Test
    @DisplayName("Restock: actorId đi vào createdBy — orderId không bao giờ thành user id")
    void restock_forwardsActorId() {
        adapter.restock(List.of(new StockDecrementCommand(2L, 1)), 7L);

        verify(restockUseCase).restock(eq(2L), eq(1), isNull(), eq(7L));
    }

    @Test
    @DisplayName("Restock actor null (system): cho qua, cột nullable")
    void restock_nullActor_allowed() {
        adapter.restock(List.of(new StockDecrementCommand(2L, 1)), null);

        verify(restockUseCase).restock(eq(2L), eq(1), isNull(), isNull());
    }

    @Test
    @DisplayName("Release/deduct: vẫn truyền orderId làm reference — không lẫn với actor")
    void releaseAndDeduct_passOrderId() {
        List<StockDecrementCommand> items = List.of(new StockDecrementCommand(2L, 1));

        adapter.release(items, 99L);
        adapter.deduct(items, 99L);

        ArgumentCaptor<List<StockDecrementCommandDto>> captor = ArgumentCaptor.forClass(List.class);
        verify(releaseStockUseCase).release(captor.capture(), eq(99L));
        assertThat(captor.getValue()).hasSize(1);
        verify(deductStockUseCase).deduct(anyList(), eq(99L));
    }
}
