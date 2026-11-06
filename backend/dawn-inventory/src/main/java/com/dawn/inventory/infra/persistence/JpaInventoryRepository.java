package com.dawn.inventory.infra.persistence;

import com.dawn.inventory.entity.Inventory;
import com.dawn.inventory.port.out.InventoryRepository;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface JpaInventoryRepository extends JpaRepository<Inventory, Long>, JpaSpecificationExecutor<Inventory>, InventoryRepository {

    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.skuId = :skuId AND i.warehouseId = :warehouseId")
    Optional<Inventory> findBySkuIdAndWarehouseIdWithLock(@Param("skuId") Long skuId, @Param("warehouseId") Long warehouseId);

    @Override
    @Query("SELECT i FROM Inventory i WHERE i.skuId IN :skuIds")
    List<Inventory> findBySkuIdIn(@Param("skuIds") List<Long> skuIds);

    // Single-warehouse compat — returns first warehouse record (for admin display)
    @Query("SELECT i FROM Inventory i WHERE i.skuId = :skuId ORDER BY i.warehouseId ASC")
    Optional<Inventory> findBySkuId(@Param("skuId") Long skuId);

    @Override
    @Query("SELECT i FROM Inventory i WHERE ( i.stockQuantity - i.reservedQuantity ) <= :threshold")
    List<Inventory> findLowStock(@Param("threshold") int threshold);

    @Override
    default Inventory save(Inventory inventory) {
        return saveAndFlush(inventory);
    }

    @Override
    default Page<Inventory> findFiltered(Long skuId, boolean lowStock, int lowStockThreshold, Pageable pageable) {
        Specification<Inventory> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (skuId != null) {
                predicates.add(cb.equal(root.get("skuId"), skuId));
            }
            if (lowStock) {
                predicates.add(cb.lessThanOrEqualTo(
                        cb.diff(root.get("stockQuantity"), root.get("reservedQuantity")),
                        lowStockThreshold));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return findAll(spec, pageable);
    }

    @QueryHints(@QueryHint(name = "org.hibernate.fetchSize", value = "500"))
    @Query("SELECT i FROM Inventory i")
    Stream<Inventory> streamAll();

}
