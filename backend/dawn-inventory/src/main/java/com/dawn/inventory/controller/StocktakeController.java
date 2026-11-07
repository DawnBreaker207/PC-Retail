package com.dawn.inventory.controller;

import com.dawn.common.response.ResponseObject;
import com.dawn.common.security.AuthorizationConstants;
import com.dawn.common.utils.SecurityUtils;
import com.dawn.inventory.controller.request.StocktakeRequest;
import com.dawn.inventory.dto.StocktakeVarianceDto;
import com.dawn.inventory.service.StocktakeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/stock-takes")
@RequiredArgsConstructor
public class StocktakeController {

    private final StocktakeService stocktakeService;

    @PostMapping
    @PreAuthorize(AuthorizationConstants.STAFF_OR_ADMIN)
    public ResponseObject<Map<String, Long>> create(@Valid @RequestBody StocktakeRequest req) {
        Long staffId = SecurityUtils.getCurrentUserId();
        Long id = stocktakeService.create(req, staffId);
        return ResponseObject.success(Map.of("id", id), "Stocktake session created");
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize(AuthorizationConstants.STAFF_OR_ADMIN)
    public ResponseObject<Void> complete(@PathVariable Long id) {
        Long staffId = SecurityUtils.getCurrentUserId();
        stocktakeService.complete(id, staffId);
        return ResponseObject.success(null, "Stocktake completed and inventory adjusted");
    }

    @GetMapping("/{id}/variances")
    @PreAuthorize(AuthorizationConstants.STAFF_OR_ADMIN)
    public ResponseObject<List<StocktakeVarianceDto>> variances(@PathVariable Long id) {
        return ResponseObject.success(stocktakeService.getVariances(id));
    }
}
