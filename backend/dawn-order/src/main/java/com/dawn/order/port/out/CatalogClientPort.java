package com.dawn.order.port.out;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface CatalogClientPort {
    Map<Long, BigDecimal> getPrices(List<Long> skuIds);
}
