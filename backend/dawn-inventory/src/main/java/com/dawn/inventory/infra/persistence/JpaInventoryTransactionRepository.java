package com.dawn.inventory.infra.persistence;

import com.dawn.inventory.entity.InventoryTransaction;
import com.dawn.inventory.port.out.InventoryTransactionRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaInventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long>, InventoryTransactionRepository {
}
