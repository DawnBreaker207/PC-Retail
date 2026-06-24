package com.dawn.ai.tools;

import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CompareProductTool {
    private final JdbcTemplate jdbcTemplate;

    @Tool("So sánh 2 sản phẩm theo thông số kỹ thuật và giá. "
            + "Nhập 2 skuId. Dùng khi người dùng muốn so sánh sản phẩm A với B.")
    public String compareProducts(Long skuId1, Long skuId2) {
        String sql = """
                SELECT s.id, s.name, s.price, s.specs::text as specs,
                       b.name as brand, c.name as category
                FROM sku s
                LEFT JOIN brand b ON b.id = s.brand_id
                LEFT JOIN category c ON c.id = s.category_id
                WHERE s.id IN (?, ?) AND s.deleted_at IS NULL
                """;

        List<String[]> products = jdbcTemplate.query(sql,
                (rs, rowNum) -> new String[]{
                        rs.getString("name"),
                        rs.getString("brand"),
                        rs.getString("category"),
                        rs.getBigDecimal("price").toPlainString(),
                        rs.getString("specs")
                }, skuId1, skuId2);

        if (products.size() < 2) {
            return "Không tìm thấy một hoặc cả hai sản phẩm để so sánh.";
        }

        String[] p1 = products.get(0);
        String[] p2 = products.get(1);

        return String.format("""
                        SO SÁNH SẢN PHẨM:
                        
                        [1] %s (%s)
                        Danh mục: %s | Giá: %s VNĐ
                        Thông số: %s
                        
                        [2] %s (%s)
                        Danh mục: %s | Giá: %s VNĐ
                        Thông số: %s
                        """,
                p1[0], p1[1], p1[2], p1[3], p1[4],
                p2[0], p2[1], p2[2], p2[3], p2[4]);
    }
}
