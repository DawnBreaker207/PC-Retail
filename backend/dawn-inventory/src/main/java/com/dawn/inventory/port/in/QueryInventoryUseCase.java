package com.dawn.inventory.port.in;

import com.dawn.common.response.ResponsePage;
import com.dawn.inventory.dto.InventoryDto;
import com.dawn.inventory.dto.InventoryTransactionDto;

public interface QueryInventoryUseCase {
    ResponsePage<InventoryDto> getAll(int page, int size, Long skuId, boolean lowStock);

    InventoryDto getBySkuId(Long skuId);

    ResponsePage<InventoryTransactionDto> getTransactions(Long skuId, int page, int size);

}
