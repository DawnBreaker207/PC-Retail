package com.dawn.inventory.infra.persistence;

import com.dawn.inventory.entity.Inventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long>, JpaSpecificationExecutor<Inventory> {
    Optional<Inventory> findBySkuId(Long skuId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.skuId = :skuId")
    Optional<Inventory> findBySkuIdWithLock(Long skuId);

    @Query("SELECT i FROM Inventory i WHERE ( i.stockQuantity - i.reservedQuantity ) <= :threshold")
    List<Inventory> findLowStock(@Param("threshold") int threshold);
}
