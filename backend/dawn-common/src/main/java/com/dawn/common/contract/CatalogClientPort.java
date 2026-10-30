package com.dawn.common.contract;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface CatalogClientPort {
    Map<Long, BigDecimal> getPrices(List<Long> skuIds);

    Map<Long, String> getNames(List<Long> skuIds);
}
