package com.dawn.reporting.infra.persistence;

import com.dawn.reporting.entity.StockTakeItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaStockTakeItemRepository extends JpaRepository<StockTakeItem, Long> {
    List<StockTakeItem> findBySessionId(Long sessionId);
}
