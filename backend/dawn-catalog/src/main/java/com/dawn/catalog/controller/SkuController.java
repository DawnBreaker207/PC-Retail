package com.dawn.catalog.controller;

import com.dawn.catalog.controller.request.SkuCreatedRequest;
import com.dawn.catalog.controller.request.SkuUpdatedRequest;
import com.dawn.catalog.dto.SkuDetailDto;
import com.dawn.catalog.dto.SkuSummaryDto;
import com.dawn.catalog.service.SkuService;
import com.dawn.common.response.ResponseObject;
import com.dawn.common.response.ResponsePage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/skus")
@RequiredArgsConstructor
public class SkuController {
    private final SkuService skuService;

    @GetMapping("")
    public ResponseObject<ResponsePage<SkuSummaryDto>> search(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String search) {
        return ResponseObject.success(
                skuService.search(page, size, brandId, categoryId, minPrice, maxPrice, search));
    }

    @GetMapping("/{id}")
    public ResponseObject<SkuDetailDto> getDetail(@PathVariable Long id) {
        return ResponseObject.success(skuService.getDetail(id));
    }

    @PostMapping("")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseObject<SkuDetailDto> create(@Valid @RequestBody SkuCreatedRequest request) {
        return ResponseObject.created(skuService.create(request));
    }

    @PutMapping("/{id}")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseObject<SkuDetailDto> update(@PathVariable Long id,
                                               @RequestBody SkuUpdatedRequest request) {
        return ResponseObject.success(skuService.update(id, request));
    }

    @DeleteMapping("/{id}")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseObject<Void> softDelete(@PathVariable Long id) {
        skuService.softDelete(id);
        return ResponseObject.deleted();
    }
}
