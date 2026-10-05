package com.dawn.inventory.infra.persistence;

import com.dawn.inventory.entity.StockReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaStockReceiptRepository extends JpaRepository<StockReceipt, Long> {
}
