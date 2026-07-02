package com.dawn.inventory.infra.persistence;

import com.dawn.inventory.entity.Inventory;
import com.dawn.inventory.port.out.InventoryRepository;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface JpaInventoryRepository extends JpaRepository<Inventory, Long>, JpaSpecificationExecutor<Inventory>, InventoryRepository {

    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.skuId = :skuId")
    Optional<Inventory> findBySkuIdWithLock(Long skuId);

    @Override
    @Query("SELECT i FROM Inventory i WHERE ( i.stockQuantity - i.reservedQuantity ) <= :threshold")
    List<Inventory> findLowStock(@Param("threshold") int threshold);

    @Override
    default Inventory save(Inventory inventory) {
        return saveAndFlush(inventory);
    }

    @QueryHints(@QueryHint(name = "org.hibernate.fetchSize", value = "500"))
    @Query("SELECT i FROM Inventory i")
    Stream<Inventory> streamAll();

}
