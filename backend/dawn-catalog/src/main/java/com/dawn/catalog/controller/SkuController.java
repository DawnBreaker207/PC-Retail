package com.dawn.catalog.controller;

import com.dawn.catalog.controller.request.SkuCreatedRequest;
import com.dawn.catalog.controller.request.SkuUpdatedRequest;
import com.dawn.catalog.dto.SkuCompareResponse;
import com.dawn.catalog.dto.SkuDetailDto;
import com.dawn.catalog.dto.SkuSummaryDto;
import com.dawn.catalog.service.SkuService;
import com.dawn.common.event.ClickstreamEvent;
import com.dawn.common.event.ClickstreamPublisher;
import com.dawn.common.response.ResponseObject;
import com.dawn.common.response.ResponsePage;
import com.dawn.common.security.AuthorizationConstants;
import com.dawn.common.utils.SecurityUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/skus")
@RequiredArgsConstructor
@Validated
public class SkuController {
    private final SkuService skuService;
    private final ClickstreamPublisher clickstreamPublisher;

    @GetMapping("")
    public ResponseObject<ResponsePage<SkuSummaryDto>> search(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") @Max(100) int size,
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

    @PostMapping("/{id}/view")
    public void view(@PathVariable Long id,
                     @RequestBody(required = false) Map<String, Object> body) {
        Map<String, Object> safe = body == null ? Map.of() : body;
        clickstreamPublisher.publish(new ClickstreamEvent(
                SecurityUtils.getCurrentUserId(),
                id,
                (String) safe.get("componentCategory"),
                safe.get("price") == null ? null : new BigDecimal(safe.get("price").toString()),
                safe.get("specs") instanceof Map<?, ?> specs ? (Map<String, Object>) specs : Map.of()));
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
            @RequestParam("ids") @NotEmpty @Size(min = 2, max = 4) List<Long> ids) {
        return ResponseObject.success(skuService.getSkusForCompare(ids));
    }
}
