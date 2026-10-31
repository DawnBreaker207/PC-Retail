package com.dawn.reporting.repository;

import com.dawn.reporting.entity.StockTakeSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaStockTakeSessionRepository extends JpaRepository<StockTakeSession, Long> {
}
