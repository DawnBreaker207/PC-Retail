package com.dawn.rating.controller;

import com.dawn.common.response.ResponseObject;
import com.dawn.common.response.ResponsePage;
import com.dawn.common.security.AuthorizationConstants;
import com.dawn.common.utils.SecurityUtils;
import com.dawn.rating.dto.RatingDto;
import com.dawn.rating.dto.RatingRequest;
import com.dawn.rating.dto.SkuRatingSummary;
import com.dawn.rating.service.RatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @PostMapping
    public ResponseObject<RatingDto> submit(@Valid @RequestBody RatingRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseObject.created(ratingService.submitRating(userId, request));
    }

    @GetMapping("/sku/{skuId}")
    public ResponseObject<ResponsePage<RatingDto>> listBysku(
            @PathVariable Long skuId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseObject.success(ratingService.getSkuRatings(skuId, pageable));
    }

    @GetMapping("/sku/{skuId}/summary")
    public ResponseObject<SkuRatingSummary> summary(@PathVariable Long skuId) {
        return ResponseObject.success(ratingService.getSkuSummary(skuId));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize(AuthorizationConstants.ADMIN_ONLY)
    public ResponseObject<Void> approve(@PathVariable Long id) {
        ratingService.approveRating(id);
        return ResponseObject.success(null);
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize(AuthorizationConstants.ADMIN_ONLY)
    public ResponseObject<Void> reject(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        ratingService.rejectRating(id, reason);
        return ResponseObject.success(null);
    }
}
