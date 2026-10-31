package com.dawn.reporting.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StockTakeQueryService {

    private final JdbcTemplate jdbc;

    @Transactional(readOnly = true)
    public List<Map<String, Object>> variances(Long sessionId) {
        return jdbc.queryForList(
                "SELECT sku_id, system_quantity, counted_quantity, (counted_quantity - system_quantity) AS variance "
                        + "FROM stock_take_item WHERE session_id = ? ORDER BY ABS(counted_quantity - system_quantity) DESC",
                sessionId);
    }
}
