package com.dawn.order.infra.persistence;

import com.dawn.order.entity.WarrantyClaim;
import com.dawn.order.entity.enums.WarrantyClaimStatus;
import com.dawn.order.port.out.WarrantyClaimRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaWarrantyClaimRepository extends JpaRepository<WarrantyClaim, Long>, WarrantyClaimRepository {

    @Override
    List<WarrantyClaim> findByWarrantyItemId(Long warrantyItemId);

    @Override
    List<WarrantyClaim> findByStatus(WarrantyClaimStatus status);
}
