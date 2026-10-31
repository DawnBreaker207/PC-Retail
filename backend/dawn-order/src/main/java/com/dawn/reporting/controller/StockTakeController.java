package com.dawn.reporting.controller;

import com.dawn.common.security.AuthorizationConstants;
import com.dawn.common.utils.SecurityUtils;
import com.dawn.reporting.entity.StockTakeSession;
import com.dawn.reporting.service.StockTakeCommandService;
import com.dawn.reporting.service.StockTakeQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/stock-takes")
@RequiredArgsConstructor
@PreAuthorize(AuthorizationConstants.ADMIN_ONLY)
public class StockTakeController {

    private final StockTakeCommandService stockTakeCommandService;
    private final StockTakeQueryService stockTakeQueryService;

    @PostMapping
    public StockTakeSession start(@RequestBody Map<String, Object> body) {
        Object raw = body.get("items");
        List<Map<String, Object>> items = raw instanceof List<?> list
                ? list.stream().map(o -> (Map<String, Object>) o).toList()
                : List.of();
        return stockTakeCommandService.start(SecurityUtils.getCurrentUserId(), items);
    }

    @PostMapping("/{id}/complete")
    public StockTakeSession complete(@PathVariable Long id) {
        return stockTakeCommandService.complete(id);
    }

    @GetMapping("/{id}/variances")
    public List<Map<String, Object>> variances(@PathVariable Long id) {
        return stockTakeQueryService.variances(id);
    }
}
