package com.dawn.inventory.service;

import com.dawn.common.exception.wrapper.InvalidRequestException;
import com.dawn.inventory.entity.StockReceipt;
import com.dawn.inventory.entity.StockReceiptItem;
import com.dawn.inventory.infra.persistence.JpaStockReceiptItemRepository;
import com.dawn.inventory.infra.persistence.JpaStockReceiptRepository;
import com.dawn.inventory.port.in.RestockUseCase;
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
class StockReceiptServiceTest {

    @Mock
    JpaStockReceiptRepository receiptRepo;
    @Mock
    JpaStockReceiptItemRepository itemRepo;
    @Mock
    RestockUseCase restockUseCase;

    StockReceiptService service;

    @BeforeEach
    void setUp() {
        service = new StockReceiptService(receiptRepo, itemRepo, restockUseCase);
        lenient().when(receiptRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private StockReceiptItem item(Long sku, int qty) {
        return StockReceiptItem.builder().receiptId(1L).skuId(sku)
                .quantity(qty).unitCost(BigDecimal.TEN).build();
    }

    @Test
    @DisplayName("Receipt rỗng: 400, không lưu gì")
    void emptyItems_badRequest() {
        assertThatThrownBy(() -> service.create(9L, List.of()))
                .isInstanceOf(InvalidRequestException.class);

        verifyNoInteractions(receiptRepo);
    }

    @Test
    @DisplayName("Item số âm: 400")
    void negativeItem_badRequest() {
        assertThatThrownBy(() -> service.create(9L, List.of(item(1L, -2))))
                .isInstanceOf(InvalidRequestException.class);

        verifyNoInteractions(receiptRepo);
    }

    @Test
    @DisplayName("Confirm lần 2 (đã CONFIRMED): idempotent, không nhập kho nữa")
    void confirmTwice_idempotent() {
        StockReceipt confirmed = StockReceipt.builder().status("CONFIRMED").build();
        confirmed.setId(1L);
        when(receiptRepo.findById(1L)).thenReturn(Optional.of(confirmed));

        StockReceipt result = service.confirm(1L, 9L);

        assertThat(result.getStatus()).isEqualTo("CONFIRMED");
        verifyNoInteractions(restockUseCase);
        verify(itemRepo, never()).findByReceiptId(anyLong());
    }

    @Test
    @DisplayName("Confirm DRAFT: nhập từng item rồi chuyển CONFIRMED")
    void confirmDraft_restocksAll() {
        StockReceipt draft = StockReceipt.builder().status("DRAFT").build();
        draft.setId(1L);
        when(receiptRepo.findById(1L)).thenReturn(Optional.of(draft));
        when(itemRepo.findByReceiptId(1L)).thenReturn(List.of(item(1L, 5), item(2L, 3)));

        StockReceipt result = service.confirm(1L, 9L);

        assertThat(result.getStatus()).isEqualTo("CONFIRMED");
        assertThat(result.getConfirmedAt()).isNotNull();
        verify(restockUseCase).restock(eq(1L), eq(5), any(), eq(9L));
        verify(restockUseCase).restock(eq(2L), eq(3), any(), eq(9L));
    }
}
