package com.dawn.ai.controller;

import com.dawn.ai.dto.AiSearchResult;
import com.dawn.ai.port.in.SearchProductUseCase;
import com.dawn.common.response.ResponseObject;
import com.dawn.common.security.AuthorizationConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class RecommendController {

    private final SearchProductUseCase searchProductUseCase;

    // Content-based: top-K gần nhất theo cosine — dùng search theo tên SKU gốc
    @GetMapping("/ai/recommendations/{skuId}")
    @PreAuthorize(AuthorizationConstants.CUSTOMER_ONLY)
    public ResponseObject<List<AiSearchResult>> recommend(@PathVariable Long skuId) {
        return ResponseObject.success(searchProductUseCase.search("sku:" + skuId, 5));
    }

    @GetMapping("/ai/similar/{skuId}")
    @PreAuthorize(AuthorizationConstants.CUSTOMER_ONLY)
    public ResponseObject<List<AiSearchResult>> similar(
            @PathVariable Long skuId,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseObject.success(searchProductUseCase.similar(skuId, limit));
    }

    // Admin dedup-check khi tạo SKU mới
    @PostMapping("/ai/dedup-check")
    @PreAuthorize(AuthorizationConstants.ADMIN_ONLY)
    public ResponseObject<List<AiSearchResult>> dedupCheck(@RequestBody Map<String, String> body) {
        return ResponseObject.success(searchProductUseCase.dedupCheck(
                body.getOrDefault("name", ""), body.getOrDefault("description", ""), 5));
    }

    @PostMapping("/ai/suggest-categories")
    @PreAuthorize(AuthorizationConstants.ADMIN_ONLY)
    public ResponseObject<List<String>> suggestCategories(@RequestBody Map<String, String> body) {
        return ResponseObject.success(searchProductUseCase.suggestCategories(
                body.getOrDefault("name", ""), body.getOrDefault("description", ""), 3));
    }
}
