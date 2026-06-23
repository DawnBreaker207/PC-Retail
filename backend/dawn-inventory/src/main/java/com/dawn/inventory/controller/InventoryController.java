package com.dawn.inventory.controller;

import com.dawn.common.response.ResponseObject;
import com.dawn.common.response.ResponsePage;
import com.dawn.inventory.controller.request.RestockRequest;
import com.dawn.inventory.dto.InventoryDto;
import com.dawn.inventory.dto.InventoryTransactionDto;
import com.dawn.inventory.port.in.QueryInventoryUseCase;
import com.dawn.inventory.port.in.RestockUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {
    private final QueryInventoryUseCase queryInventoryUseCase;
    private final RestockUseCase restockUseCase;

    @GetMapping
    public ResponseObject<ResponsePage<InventoryDto>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long skuId,
            @RequestParam(defaultValue = "false") boolean lowStock) {
        return ResponseObject.success(queryInventoryUseCase.getAll(page, size, skuId, lowStock));
    }

    @GetMapping("/{skuId}")
    public ResponseObject<InventoryDto> getBySkuId(@PathVariable Long skuId) {
        return ResponseObject.success(queryInventoryUseCase.getBySkuId(skuId));
    }

    @PutMapping("/{skuId}/restock")
    public ResponseObject<Void> restock(
            @PathVariable Long skuId,
            @Valid @RequestBody RestockRequest req) {
        restockUseCase.restock(skuId, req.quantity(), req.unitCost(), null);
        return ResponseObject.success(null, "Restocked successfully");
    }


    @GetMapping("/{skuId}/transactions")
    public ResponseObject<ResponsePage<InventoryTransactionDto>> getTransactions(
            @PathVariable Long skuId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseObject.success(queryInventoryUseCase.getTransactions(skuId, page, size));
    }
}
