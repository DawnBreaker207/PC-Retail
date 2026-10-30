package com.dawn.promotion.port.out;

import com.dawn.promotion.entity.Voucher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface VoucherRepository {
    boolean existsByCodeAndDeletedAtIsNull(String code);

    Optional<Voucher> findByCodeAndDeletedAtIsNull(String code);

    List<Voucher> findByCodeInAndDeletedAtIsNull(List<String> codes);

    List<Voucher> findWithLockByCodeInAndDeletedAtIsNull(List<String> codes);

    Page<Voucher> findAllByDeletedAtIsNull(Pageable pageable);

    boolean hasActiveFlashSale(List<Long> skuIds);

    Voucher save(Voucher voucher);

    Optional<Voucher> findById(Long id);

}
