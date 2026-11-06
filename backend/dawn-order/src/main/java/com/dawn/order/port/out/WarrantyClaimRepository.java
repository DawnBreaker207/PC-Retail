package com.dawn.order.port.out;

import com.dawn.order.entity.WarrantyClaim;
import com.dawn.order.entity.enums.WarrantyClaimStatus;

import java.util.List;
import java.util.Optional;

public interface WarrantyClaimRepository {
    WarrantyClaim save(WarrantyClaim claim);
    Optional<WarrantyClaim> findById(Long id);
    List<WarrantyClaim> findByWarrantyItemId(Long warrantyItemId);
    List<WarrantyClaim> findByStatus(WarrantyClaimStatus status);
}
