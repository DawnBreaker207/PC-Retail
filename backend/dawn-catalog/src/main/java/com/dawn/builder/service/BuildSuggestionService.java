package com.dawn.builder.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Gợi ý cấu hình có sẵn (build_template) theo vector sở thích danh mục + ngân sách.
 * Chỉ xét template ACTIVE có default SKU còn bán; không tự sinh linh kiện mới.
 */
@Service
@RequiredArgsConstructor
public class BuildSuggestionService {

    private final JdbcTemplate jdbcTemplate;

    public record Component(String slotType, Long skuId, String name, BigDecimal price, String category) {
    }

    public record SuggestedBuild(
            Long templateId,
            String name,
            String description,
            BigDecimal totalPrice,
            double score,
            List<Component> components) {
    }

    public List<SuggestedBuild> suggest(Map<String, Double> categoryWeights, BigDecimal budget, int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 10);
        List<Map<String, Object>> templates = jdbcTemplate.queryForList(
                "SELECT id, name, description FROM build_template WHERE status = 'ACTIVE'");

        List<SuggestedBuild> builds = new ArrayList<>();
        for (Map<String, Object> template : templates) {
            Long templateId = ((Number) template.get("id")).longValue();
            List<Component> components = jdbcTemplate.queryForList("""
                            SELECT bts.slot_type, s.id, s.name, s.price, c.name AS category
                            FROM build_template_slot bts
                            JOIN sku s ON s.id = bts.default_sku_id AND s.deleted_at IS NULL
                            LEFT JOIN category c ON c.id = s.category_id
                            WHERE bts.template_id = ?
                            """, templateId).stream()
                    .map(row -> new Component(
                            String.valueOf(row.get("slot_type")),
                            ((Number) row.get("id")).longValue(),
                            String.valueOf(row.get("name")),
                            (BigDecimal) row.get("price"),
                            (String) row.get("category")))
                    .toList();
            if (components.isEmpty()) {
                continue;
            }
            BigDecimal total = components.stream()
                    .map(Component::price)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (budget != null && total.compareTo(budget) > 0) {
                continue;
            }
            double score = components.stream()
                    .mapToDouble(c -> categoryWeights == null || c.category() == null
                            ? 0.0
                            : categoryWeights.getOrDefault(c.category(), 0.0))
                    .sum();
            builds.add(new SuggestedBuild(
                    templateId,
                    (String) template.get("name"),
                    (String) template.get("description"),
                    total,
                    score,
                    components));
        }

        builds.sort(Comparator.comparingDouble(SuggestedBuild::score).reversed()
                .thenComparing(SuggestedBuild::totalPrice));
        return builds.stream().limit(safeLimit).toList();
    }
}
