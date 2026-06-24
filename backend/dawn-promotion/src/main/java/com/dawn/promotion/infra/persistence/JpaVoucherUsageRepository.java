package com.dawn.promotion.infra.persistence;

import com.dawn.promotion.entity.VoucherUsage;
import com.dawn.promotion.port.out.VoucherUsageRepositoryPort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaVoucherUsageRepository extends JpaRepository<VoucherUsage, Long>, VoucherUsageRepositoryPort { }
