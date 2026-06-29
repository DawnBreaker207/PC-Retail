package com.dawn.promotion.port.out;

import com.dawn.promotion.entity.VoucherUsage;

public interface VoucherUsageRepository {
    boolean existsByVoucherIdAndUserId(Long voucherId, Long userId);

    VoucherUsage save(VoucherUsage usage);
}
