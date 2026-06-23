package com.dawn.order.repository;

import com.dawn.order.entity.VoucherUsage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoucherUsageRepository extends JpaRepository<VoucherUsage, Long> {
    boolean existsByVoucherIdAndUserId(Long voucherId, Long userId);
}
