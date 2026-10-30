package com.dawn.reporting.service;

import com.dawn.common.exception.wrapper.ConflictException;
import com.dawn.common.exception.wrapper.ResourceNotFoundException;
import com.dawn.reporting.entity.StockTakeItem;
import com.dawn.reporting.entity.StockTakeSession;
import com.dawn.reporting.infra.persistence.JpaStockTakeItemRepository;
import com.dawn.reporting.infra.persistence.JpaStockTakeSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StockTakeService {

    private final JpaStockTakeSessionRepository sessionRepo;
    private final JpaStockTakeItemRepository itemRepo;
    private final JdbcTemplate jdbc;

    @Transactional
    public StockTakeSession start(Long staffId, List<Map<String, Object>> counts) {
        if (counts == null || counts.isEmpty()) {
            throw new com.dawn.common.exception.wrapper.InvalidRequestException("Count list must not be empty");
        }
        StockTakeSession session = sessionRepo.save(StockTakeSession.builder()
                .startedBy(staffId).status("IN_PROGRESS").build());
        for (Map<String, Object> c : counts) {
            Long skuId = Long.valueOf(String.valueOf(c.get("skuId")));
            int counted = Integer.parseInt(String.valueOf(c.get("countedQuantity")));
            if (counted < 0) {
                throw new com.dawn.common.exception.wrapper.InvalidRequestException("Counted quantity must be >= 0");
            }
            Integer systemQty = jdbc.queryForObject(
                    "SELECT COALESCE(stock_quantity,0)-COALESCE(reserved_quantity,0) FROM inventory WHERE sku_id=?",
                    Integer.class, skuId);
            int system = systemQty == null ? 0 : systemQty;
            itemRepo.save(StockTakeItem.builder()
                    .sessionId(session.getId()).skuId(skuId)
                    .systemQuantity(system).countedQuantity(counted).build());
        }
        return session;
    }

    @Transactional
    public StockTakeSession complete(Long sessionId) {
        StockTakeSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("StockTakeSession not found: " + sessionId));
        if (!"IN_PROGRESS".equals(session.getStatus())) {
            throw new ConflictException("Session already " + session.getStatus());
        }
        session.setStatus("COMPLETED");
        session.setCompletedAt(Instant.now());
        return sessionRepo.save(session);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> variances(Long sessionId) {
        return jdbc.queryForList(
                "SELECT sku_id, system_quantity, counted_quantity, (counted_quantity - system_quantity) AS variance "
                        + "FROM stock_take_item WHERE session_id = ? ORDER BY ABS(counted_quantity - system_quantity) DESC",
                sessionId);
    }
}
