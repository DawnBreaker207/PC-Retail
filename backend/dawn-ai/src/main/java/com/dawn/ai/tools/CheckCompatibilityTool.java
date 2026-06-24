package com.dawn.ai.tools;

import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CheckCompatibilityTool {

    private final JdbcTemplate jdbcTemplate;

    @Tool("Kiểm tra 2 linh kiện PC có tương thích nhau không dựa trên compatibility rules. "
            + "Nhập 2 skuId. Dùng khi người dùng hỏi CPU A có dùng được với Mainboard B không.")
    public String checkCompatibility(Long skuId1, Long skuId2) {
        // Lấy category của 2 SKU
        String catSql = "SELECT id, name, category_id FROM sku WHERE id IN (?, ?) AND deleted_at IS NULL";
        List<Object[]> skus = jdbcTemplate.query(catSql,
                (rs, rowNum) -> new Object[]{rs.getLong("id"), rs.getString("name"), rs.getLong("category_id")},
                skuId1, skuId2);

        if (skus.size() < 2) {
            return "Không tìm thấy một hoặc cả hai sản phẩm.";
        }

        Long catA = (Long) skus.get(0)[2];
        Long catB = (Long) skus.get(1)[2];

        // Query compatibility rules giữa 2 category
        String ruleSql = """
                SELECT rule_type, attribute_key, expected_value, description
                FROM compatibility_rule
                WHERE (source_category_id = ? AND target_category_id = ?)
                   OR (source_category_id = ? AND target_category_id = ?)
                AND deleted_at IS NULL
                """;

        List<String> rules = jdbcTemplate.query(ruleSql,
                (rs, rowNum) -> String.format("[%s] %s: %s — %s",
                        rs.getString("rule_type"),
                        rs.getString("attribute_key"),
                        rs.getString("expected_value"),
                        rs.getString("description")),
                catA, catB, catB, catA);

        if (rules.isEmpty()) {
            return String.format("Không có quy tắc tương thích nào được định nghĩa giữa 2 sản phẩm này. "
                    + "Kiểm tra thủ công hoặc liên hệ nhân viên tư vấn.");
        }

        return String.format("Quy tắc tương thích giữa [%s] và [%s]:\n%s",
                skus.get(0)[1], skus.get(1)[1],
                String.join("\n", rules));
    }
}
