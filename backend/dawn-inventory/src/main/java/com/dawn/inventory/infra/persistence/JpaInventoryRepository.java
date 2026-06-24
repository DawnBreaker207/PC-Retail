package com.dawn.inventory.infra.persistence;

import com.dawn.inventory.entity.Inventory;
import com.dawn.inventory.port.out.InventoryRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaInventoryRepository extends JpaRepository<Inventory, Long>, JpaSpecificationExecutor<Inventory>, InventoryRepository {

    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.skuId = :skuId")
    Optional<Inventory> findBySkuIdWithLock(Long skuId);

    @Override
    @Query("SELECT i FROM Inventory i WHERE ( i.stockQuantity - i.reservedQuantity ) <= :threshold")
    List<Inventory> findLowStock(@Param("threshold") int threshold);
}
