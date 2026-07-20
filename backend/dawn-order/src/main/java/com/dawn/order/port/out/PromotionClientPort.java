package com.dawn.order.port.out;

import com.dawn.order.dto.VoucherApplyResult;

import java.math.BigDecimal;
import java.util.List;

public interface PromotionClientPort {
    void validateStack(List<Long> cartSkuIds, List<String> voucherCodes);

    VoucherApplyResult validate(String code, BigDecimal orderTotal);

    void reservePromotions(List<String> voucherCodes, List<Long> flashSaleSkuIds);

    void rollBackPromotions(List<String> voucherCodes, List<Long> flashSaleSkuIds);

    void recordUsage(Long userId, Long orderId, List<String> voucherCodes, BigDecimal orderTotal);
}
