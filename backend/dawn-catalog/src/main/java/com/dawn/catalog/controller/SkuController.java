package com.dawn.catalog.controller;

import com.dawn.catalog.controller.request.SkuCreatedRequest;
import com.dawn.catalog.controller.request.SkuUpdatedRequest;
import com.dawn.catalog.dto.SkuCompareResponse;
import com.dawn.catalog.dto.SkuDetailDto;
import com.dawn.catalog.dto.SkuSummaryDto;
import com.dawn.catalog.service.SkuService;
import com.dawn.common.response.ResponseObject;
import com.dawn.common.response.ResponsePage;
import com.dawn.common.security.AuthorizationConstants;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

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
    @PreAuthorize(AuthorizationConstants.ADMIN_ONLY)
    public ResponseObject<SkuDetailDto> create(@Valid @RequestBody SkuCreatedRequest request) {
        return ResponseObject.created(skuService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize(AuthorizationConstants.ADMIN_ONLY)
    public ResponseObject<SkuDetailDto> update(
            @PathVariable Long id,
            @Valid @RequestBody SkuUpdatedRequest request) {
        return ResponseObject.success(skuService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(AuthorizationConstants.ADMIN_ONLY)
    public ResponseObject<Void> softDelete(@PathVariable Long id) {
        skuService.softDelete(id);
        return ResponseObject.deleted();
    }

    @GetMapping("/compare")
    public ResponseObject<List<SkuCompareResponse>> compare(
            @RequestParam("ids") @NotEmpty @Size(min = 2) List<Long> ids) {
        return ResponseObject.success(skuService.getSkusForCompare(ids));
    }
}
