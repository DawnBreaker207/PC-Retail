package com.dawn.reporting.infra.persistence;

import com.dawn.reporting.entity.PurchaseOrder;
import com.dawn.reporting.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaSupplierRepository extends JpaRepository<Supplier, Long> {
}
