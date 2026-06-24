package com.dawn.ai.tools;

import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class CheckVoucherTool {
    private final JdbcTemplate jdbcTemplate;

    @Tool("Kiểm tra mã giảm giá (voucher) còn hiệu lực không. "
            + "Dùng khi người dùng hỏi voucher X còn dùng được không, còn bao nhiêu lượt.")
    public String checkVoucher(String voucherCode) {
        String sql = """
                SELECT code, discount_type, discount_value, min_order_value,
                       remaining_quantity, valid_from, valid_until, status
                FROM voucher
                WHERE code = ? AND deleted_at IS NULL
                """;

        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                String status = rs.getString("status");
                Instant now = Instant.now();
                Instant validFrom = rs.getTimestamp("valid_from").toInstant();
                Instant validUntil = rs.getTimestamp("valid_until").toInstant();
                int remaining = rs.getInt("remaining_quantity");

                if (!"ACTIVE".equals(status)) {
                    return String.format("Voucher %s không còn hoạt động (trạng thái: %s).", voucherCode, status);
                }
                if (now.isBefore(validFrom)) {
                    return String.format("Voucher %s chưa đến thời gian hiệu lực.", voucherCode);
                }
                if (now.isAfter(validUntil)) {
                    return String.format("Voucher %s đã hết hạn.", voucherCode);
                }
                if (remaining <= 0) {
                    return String.format("Voucher %s đã hết lượt sử dụng.", voucherCode);
                }
                return String.format(
                        "Voucher %s còn hiệu lực | Giảm: %s %s | Đơn tối thiểu: %,.0f VNĐ | Còn lại: %d lượt",
                        voucherCode,
                        rs.getString("discount_type"),
                        rs.getString("discount_value"),
                        rs.getBigDecimal("min_order_value"),
                        remaining
                );
            }, voucherCode);
        } catch (Exception e) {
            return "Không tìm thấy voucher: " + voucherCode;
        }
    }
}
