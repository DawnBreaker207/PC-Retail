package com.dawn.ai.tools;

import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetOrderStatusTool {
    private final JdbcTemplate jdbcTemplate;

    @Tool("Tra cứu trạng thái đơn hàng theo mã đơn hàng (orderId). "
            + "Dùng khi người dùng hỏi đơn của họ đang ở đâu, đã giao chưa.")
    public String getOrderStatus(Long orderId) {
        String sql = """
                SELECT o.id, o.status as order_status,
                       so.status as suborder_status, so.carrier::text as carrier_info,
                       o.created_at
                FROM orders o
                LEFT JOIN sub_order so ON so.order_id = o.id
                WHERE o.id = ?
                LIMIT 1
                """;

        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) ->
                    String.format("Đơn #%d | Trạng thái: %s | Vận chuyển: %s | Đặt lúc: %s",
                            rs.getLong("id"),
                            rs.getString("order_status"),
                            rs.getString("carrier_info") != null ? rs.getString("carrier_info") : "Chưa có thông tin",
                            rs.getTimestamp("created_at").toLocalDateTime().toLocalDate()
                    ), orderId);
        } catch (Exception e) {
            return "Không tìm thấy đơn hàng #" + orderId;
        }
    }
}
