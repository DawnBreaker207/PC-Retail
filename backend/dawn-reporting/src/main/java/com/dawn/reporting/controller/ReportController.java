package com.dawn.reporting.controller;

import com.dawn.common.security.AuthorizationConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/reports")
@RequiredArgsConstructor
@PreAuthorize(AuthorizationConstants.ADMIN_ONLY)
public class ReportController {

    private final JdbcTemplate jdbc;

    // ponytail: JDBC trực tiếp thay repository — báo cáo chỉ đọc, không cần entity
    @GetMapping("/revenue")
    public List<Map<String, Object>> revenue(@RequestParam String from, @RequestParam String to) {
        return jdbc.queryForList("""
                SELECT date_trunc('day', o.created_at) AS day, SUM(so.final_amount) AS revenue
                FROM orders o JOIN sub_order so ON so.order_id = o.id
                WHERE so.status = 'DELIVERED' AND o.created_at BETWEEN ?::timestamptz AND ?::timestamptz
                GROUP BY 1 ORDER BY 1
                """, from, to);
    }

    @GetMapping("/top-products")
    public List<Map<String, Object>> topProducts(@RequestParam String from, @RequestParam String to,
                                                 @RequestParam(defaultValue = "10") int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 100);
        return jdbc.queryForList("""
                SELECT oi.sku_id, SUM(oi.quantity) AS qty, SUM(oi.unit_price * oi.quantity) AS revenue
                FROM order_item oi JOIN sub_order so ON so.id = oi.sub_order_id
                JOIN orders o ON o.id = so.order_id
                WHERE so.status = 'DELIVERED' AND o.created_at BETWEEN ?::timestamptz AND ?::timestamptz
                GROUP BY oi.sku_id ORDER BY revenue DESC LIMIT ?
                """, from, to, safeLimit);
    }

    // ponytail: CSV thuần text thay Excel lib — đủ cho kế toán, thêm lib khi cần xlsx thật
    @GetMapping(value = "/export", produces = "text/csv")
    public String export(@RequestParam String from, @RequestParam String to,
                         @RequestParam(defaultValue = "revenue") String type) {
        StringBuilder csv = new StringBuilder();
        if ("top-products".equals(type)) {
            csv.append("sku_id,qty,revenue\n");
            for (Map<String, Object> r : topProducts(from, to, 100)) {
                csv.append(r.get("sku_id")).append(',')
                        .append(r.get("qty")).append(',')
                        .append(r.get("revenue")).append('\n');
            }
        } else {
            csv.append("day,revenue\n");
            for (Map<String, Object> r : revenue(from, to)) {
                csv.append(r.get("day")).append(',')
                        .append(r.get("revenue")).append('\n');
            }
        }
        return csv.toString();
    }

    @GetMapping("/invoice-pending")
    public List<Map<String, Object>> invoicePending() {
        return jdbc.queryForList("""
                SELECT o.id AS order_id, o.total_amount, o.tax_info, o.created_at
                FROM orders o
                WHERE o.invoice_requested = true
                ORDER BY o.created_at DESC LIMIT 100
                """);
    }
}
