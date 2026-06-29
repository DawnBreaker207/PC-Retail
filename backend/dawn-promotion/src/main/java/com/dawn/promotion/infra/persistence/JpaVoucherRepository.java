package com.dawn.promotion.infra.persistence;

import com.dawn.promotion.entity.Voucher;
import com.dawn.promotion.port.out.VoucherRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface JpaVoucherRepository extends JpaRepository<Voucher, Long>, VoucherRepository {
    @Override
    default boolean hasActiveFlashSale(List<Long> skuIds) {
        return false;
    }


}
