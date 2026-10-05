package com.dawn.inventory.infra.persistence;

import com.dawn.inventory.entity.StockReceiptItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaStockReceiptItemRepository extends JpaRepository<StockReceiptItem, Long> {
    List<StockReceiptItem> findByReceiptId(Long receiptId);
}
