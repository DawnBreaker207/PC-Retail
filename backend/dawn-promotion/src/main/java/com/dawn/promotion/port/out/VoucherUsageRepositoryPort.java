package com.dawn.promotion.port.out;

import com.dawn.promotion.entity.VoucherUsage;

import java.math.BigDecimal;

public interface VoucherUsageRepositoryPort {
    boolean existsByVoucherIdAndUserId(Long voucherId, Long userId);


    VoucherUsage save(VoucherUsage usage);
}
