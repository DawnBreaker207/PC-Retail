package com.dawn.promotion.dto;

import java.util.List;

public interface PromotionCheckoutPort {

    void reservePromotions(List<String> voucherCodes, List<Long> flashSaleSkuIds);

    void rollbackPromotions(List<String> voucherCodes, List<Long> flashSaleSkuIds);

    void validateStack(List<Long> cartSkuIds, List<String> voucherCodes);

    void recordUsage(Long userId, Long orderId, List<String> voucherCodes);
}
