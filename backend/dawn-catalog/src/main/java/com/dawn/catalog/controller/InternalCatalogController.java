package com.dawn.catalog.controller;

import com.dawn.common.contract.CatalogQueryPort;
import com.dawn.common.contract.SkuPriceInfo;
import com.dawn.catalog.entity.Sku;
import com.dawn.catalog.repository.SkuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** API nội bộ cho order-service/cart; bảo vệ bằng X-Internal-Token. */
@RestController
@RequestMapping("/internal/catalog")
@RequiredArgsConstructor
public class InternalCatalogController {

    private final SkuRepository skuRepository;
    private final CatalogQueryPort catalogQueryPort;

    @PostMapping("/prices")
    public Map<Long, BigDecimal> prices(@RequestBody List<Long> skuIds) {
        return skuRepository.findAllActiveByIds(skuIds).stream()
                .collect(Collectors.toMap(Sku::getId, Sku::getPrice));
    }

    @PostMapping("/names")
    public Map<Long, String> names(@RequestBody List<Long> skuIds) {
        return skuRepository.findAllActiveByIds(skuIds).stream()
                .collect(Collectors.toMap(Sku::getId, Sku::getName));
    }

    @PostMapping("/details")
    public Map<Long, SkuPriceInfo> details(@RequestBody List<Long> skuIds) {
        return catalogQueryPort.getSkuDetailsMap(skuIds);
    }
}
