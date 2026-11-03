package com.dawn.ai.controller;

import com.dawn.ai.dto.AiSearchResult;
import com.dawn.ai.optimizer.BuildOptimizerService;
import com.dawn.ai.port.in.SearchProductUseCase;
import com.dawn.ai.service.DemandForecastService;
import com.dawn.ai.service.TrendingService;
import com.dawn.common.response.ResponseObject;
import com.dawn.common.security.AuthorizationConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AIController {

    private final SearchProductUseCase searchProductUseCase;
    private final TrendingService trendingService;
    private final BuildOptimizerService buildOptimizerService;

    @GetMapping("/search")
    public ResponseObject<List<AiSearchResult>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseObject.success(searchProductUseCase.search(q, Math.min(limit, 20)));
    }

    @GetMapping("/trending")
    public ResponseObject<List<Long>> trending(
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseObject.success(trendingService.getTopTrending(Math.min(limit, 50)));
    }

    @PostMapping("/build/optimize")
    public ResponseObject<List<BuildOptimizerService.BuildResult>> optimizeBuild(
            @RequestBody BuildOptimizerService.OptimizeRequest request) {
        return ResponseObject.success(buildOptimizerService.optimize(request));
    }
}
