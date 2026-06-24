package com.dawn.promotion.infra.bridge;

import com.dawn.order.dto.VoucherApplyResult;
import com.dawn.order.port.out.PromotionClientPort;
import com.dawn.promotion.dto.PromotionCheckoutPort;
import com.dawn.promotion.dto.VoucherValidationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PromotionClientAdapter implements PromotionClientPort {

    private final PromotionCheckoutPort promotionCheckout;

    @Override
    public void validateStack(List<Long> cartSkuIds, List<String> voucherCodes) {
        promotionCheckout.validateStack(cartSkuIds, voucherCodes);
    }

    @Override
    public VoucherApplyResult validate(String code, BigDecimal orderTotal) {
        VoucherValidationResult result = promotionCheckout.validate(code, orderTotal);
        return result.valid() ?
                VoucherApplyResult.ok(result.discountAmount()) :
                VoucherApplyResult.fail(result.errorMessage());

    }

    @Override
    public void reservePromotions(List<String> voucherCodes, List<Long> flashSaleSkuIds) {
        promotionCheckout.reservePromotions(voucherCodes, flashSaleSkuIds);
    }

    @Override
    public void rollBackPromotions(List<String> voucherCodes, List<Long> flashSaleSkuIds) {
        promotionCheckout.rollbackPromotions(voucherCodes, flashSaleSkuIds);
    }

    @Override
    public void recordUsage(Long userId, Long orderId, List<String> voucherCodes) {
        promotionCheckout.recordUsage(userId, orderId, voucherCodes);
    }
}
