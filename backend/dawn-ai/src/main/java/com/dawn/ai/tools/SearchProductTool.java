package com.dawn.ai.tools;

import com.dawn.ai.dto.EmbeddingSearchResult;
import com.dawn.ai.port.out.EmbeddingPort;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SearchProductTool {
    private final EmbeddingPort embeddingPort;
    private final JdbcTemplate jdbcTemplate;

    @Tool("Tìm kiếm sản phẩm PC theo mô tả tự nhiên. Dùng khi người dùng hỏi về sản phẩm, "
            + "linh kiện, laptop theo tính năng hoặc nhu cầu sử dụng.")
    public String searchProducts(String query) {
        List<EmbeddingSearchResult> results = embeddingPort.searchSimilar(query, 5);

        if (results.isEmpty()) {
            return "Không tìm thấy sản phẩm phù hợp.";
        }

        List<Long> skuIds = results.stream().map(EmbeddingSearchResult::skuId).toList();

        // Lấy thông tin SKU từ DB — native SQL, không import entity catalog
        String inClause = skuIds.stream().map(String::valueOf).collect(Collectors.joining(","));
        String sql = String.format("""
                SELECT s.id, s.name, s.price, b.name as brand, s.stock_quantity
                FROM sku s LEFT JOIN brand b ON b.id = s.brand_id
                WHERE s.id IN (%s) AND s.deleted_at IS NULL
                """, inClause);

        List<String> rows = jdbcTemplate.query(sql, (rs, rowNum) ->
                String.format("- %s (%s) | Giá: %,.0f VNĐ | Tồn kho: %d",
                        rs.getString("name"),
                        rs.getString("brand"),
                        rs.getBigDecimal("price"),
                        rs.getInt("stock_quantity"))
        );

        return "Sản phẩm tìm được:\n" + String.join("\n", rows);
    }
}
