package com.dawn.promotion.controller;

import com.dawn.common.response.ResponseObject;
import com.dawn.common.security.AuthorizationConstants;
import com.dawn.promotion.entity.FlashSaleCampaign;
import com.dawn.promotion.entity.FlashSaleItem;
import com.dawn.promotion.service.FlashSaleCommandService;
import com.dawn.promotion.service.FlashSaleQueryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/promotion/flash-sales")
@RequiredArgsConstructor
public class FlashSaleController {

    private final FlashSaleQueryService flashSaleQueryService;
    private final FlashSaleCommandService flashSaleCommandService;

    // --- Public ---

    @GetMapping("/active")
    public ResponseObject<List<FlashSaleCampaign>> active() {
        return ResponseObject.success(flashSaleQueryService.getActiveCampaigns());
    }

    // --- Admin CRUD ---

    @PostMapping("/admin")
    @PreAuthorize(AuthorizationConstants.ADMIN_ONLY)
    public ResponseObject<FlashSaleCampaign> create(@Valid @RequestBody CreateCampaignRequest req) {
        return ResponseObject.success(
                flashSaleCommandService.createCampaign(req.name(), req.startTime(), req.endTime()),
                "Campaign created");
    }

    @PostMapping("/admin/{campaignId}/items")
    @PreAuthorize(AuthorizationConstants.ADMIN_ONLY)
    public ResponseObject<FlashSaleItem> addItem(
            @PathVariable Long campaignId,
            @Valid @RequestBody AddItemRequest req) {
        return ResponseObject.success(
                flashSaleCommandService.addItem(campaignId, req.skuId(), req.flashPrice(), req.quantityLimit()),
                "Item added");
    }

    @GetMapping("/admin/{campaignId}/items")
    @PreAuthorize(AuthorizationConstants.STAFF_OR_ADMIN)
    public ResponseObject<List<FlashSaleItem>> items(@PathVariable Long campaignId) {
        return ResponseObject.success(flashSaleCommandService.getItems(campaignId));
    }

    @DeleteMapping("/admin/items/{itemId}")
    @PreAuthorize(AuthorizationConstants.ADMIN_ONLY)
    public ResponseObject<Void> removeItem(@PathVariable Long itemId) {
        flashSaleCommandService.removeItem(itemId);
        return ResponseObject.success(null, "Item removed");
    }

    @DeleteMapping("/admin/{campaignId}")
    @PreAuthorize(AuthorizationConstants.ADMIN_ONLY)
    public ResponseObject<Void> deleteCampaign(@PathVariable Long campaignId) {
        flashSaleCommandService.deleteCampaign(campaignId);
        return ResponseObject.success(null, "Campaign deleted");
    }

    // --- Request records ---

    record CreateCampaignRequest(
            @NotBlank String name,
            @NotNull Instant startTime,
            @NotNull Instant endTime
    ) {}

    record AddItemRequest(
            @NotNull Long skuId,
            @NotNull @Positive BigDecimal flashPrice,
            @Positive int quantityLimit
    ) {}
}
