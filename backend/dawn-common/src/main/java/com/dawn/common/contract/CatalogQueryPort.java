package com.dawn.common.contract;

import java.util.List;
import java.util.Map;

public interface CatalogQueryPort {
    Map<Long, SkuPriceInfo> getSkuDetailsMap(List<Long> skuIds);
}
