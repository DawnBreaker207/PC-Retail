package com.dawn.promotion.infra.persistence;

import com.dawn.promotion.entity.Voucher;
import com.dawn.promotion.port.out.VoucherRepository;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaVoucherRepository extends JpaRepository<Voucher, Long>, VoucherRepository {

    @Query("""
            SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END
            FROM FlashSaleItem i
            JOIN FlashSaleCampaign c ON c.id = i.campaignId
            WHERE i.skuId IN :skuIds
            AND c.status = 'ACTIVE'
            """)
    @Override
    boolean hasActiveFlashSale(@Param("skuIds") List<Long> skuIds);
}
