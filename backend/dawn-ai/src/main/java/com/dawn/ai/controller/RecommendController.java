package com.dawn.ai.controller;

import com.dawn.ai.dto.AiSearchResult;
import com.dawn.ai.port.in.SearchProductUseCase;
import com.dawn.ai.service.CooccurrenceService;
import com.dawn.ai.service.DemandForecastService;
import com.dawn.ai.service.UserPreferenceService;
import com.dawn.ai.service.BuildSuggestionService;
import com.dawn.common.response.ResponseObject;
import com.dawn.common.security.AuthorizationConstants;
import com.dawn.common.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class RecommendController {

    private final SearchProductUseCase searchProductUseCase;
    private final UserPreferenceService userPreferenceService;
    private final BuildSuggestionService buildSuggestionService;
    private final CooccurrenceService cooccurrenceService;
    private final DemandForecastService demandForecastService;

    // Spec-based nearest neighbors via pgvector
    @GetMapping("/ai/recommendations/{skuId}")
    public ResponseObject<List<AiSearchResult>> recommend(
            @PathVariable Long skuId,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseObject.success(searchProductUseCase.similar(skuId, Math.min(limit, 10)));
    }

    @GetMapping("/ai/forecast/{skuId}")
    @PreAuthorize(AuthorizationConstants.ADMIN_ONLY)
    public ResponseObject<String> forecast(@PathVariable Long skuId) {
        // Forecast is scheduled — trigger on demand returns last computed result.
        // Full async report is in Kafka low-stock-events topic.
        return ResponseObject.success("Dự báo nhu cầu đang được xử lý qua scheduler lúc 2:00 sáng. Xem log Kafka topic: low-stock-events cho SKU " + skuId);
    }

    @GetMapping("/ai/similar/{skuId}")
    @PreAuthorize(AuthorizationConstants.CUSTOMER_ONLY)
    public ResponseObject<List<AiSearchResult>> similar(
            @PathVariable Long skuId,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseObject.success(searchProductUseCase.similar(skuId, limit));
    }

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

    @GetMapping("/ai/recommendations/collab/{skuId}")
    public ResponseObject<List<Long>> collaborativeRecommend(
            @PathVariable Long skuId,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseObject.success(cooccurrenceService.getRecommendations(skuId, Math.min(limit, 10)));
    }

    // falls back to price-ordering when user has no preference weights yet
    @GetMapping("/ai/build-suggestions")
    public ResponseObject<List<BuildSuggestionService.SuggestedBuild>> suggestBuilds(
            @RequestParam(required = false) BigDecimal budget,
            @RequestParam(defaultValue = "3") int limit) {
        Long userId = SecurityUtils.getCurrentUserId();
        Map<String, Double> weights = userId == null ? Map.of() : userPreferenceService.weights(userId);
        return ResponseObject.success(buildSuggestionService.suggest(weights, budget, limit));
    }
}
