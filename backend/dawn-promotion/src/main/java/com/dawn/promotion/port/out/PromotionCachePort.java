package com.dawn.promotion.port.out;

import java.util.List;

public interface PromotionCachePort {
    void setVoucherRemaining(String code, int quantity);
    void deleteVoucher(String code);
    Integer getVoucherRemaining(String code);

    void decrementPromotions(List<String> voucherCodes, List<Long> flashSaleSkuIds);
    void incrementPromotions(List<String> voucherCodes, List<Long> flashSaleSkuIds);
}
