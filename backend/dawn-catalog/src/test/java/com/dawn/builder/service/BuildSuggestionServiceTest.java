package com.dawn.builder.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import com.dawn.builder.repository.JpaBuildTemplateRepository;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BuildSuggestionServiceTest {

    @Mock
    JdbcTemplate jdbcTemplate;

    @Mock
    JpaBuildTemplateRepository templateRepository;

    @Test
    @DisplayName("suggest: ưu tiên danh mục sở thích, lọc theo ngân sách")
    void suggest_ranksByPreferenceAndBudget() {
        when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of(
                Map.of("id", 1L, "name", "Budget", "description", "rẻ"),
                Map.of("id", 2L, "name", "Gaming", "description", "mạnh"),
                Map.of("id", 3L, "name", "TooExpensive", "description", "đắt")));
        when(jdbcTemplate.queryForList(anyString(), eq(1L))).thenReturn(List.of(
                component("CPU", 11L, "cpu rẻ", "100.00", "RAM")));
        when(jdbcTemplate.queryForList(anyString(), eq(2L))).thenReturn(List.of(
                component("CPU", 21L, "cpu mạnh", "300.00", "CPU"),
                component("GPU", 22L, "gpu", "400.00", "VGA")));
        when(jdbcTemplate.queryForList(anyString(), eq(3L))).thenReturn(List.of(
                component("CPU", 31L, "cpu đắt", "1500.00", "CPU")));

        List<BuildQueryService.SuggestedBuild> result = new BuildQueryService(jdbcTemplate, templateRepository)
                .suggest(Map.of("CPU", 2.0), new BigDecimal("1000.00"), 3);

        assertThat(result).extracting(BuildQueryService.SuggestedBuild::name)
                .containsExactly("Gaming", "Budget");
        assertThat(result.getFirst().score()).isEqualTo(2.0);
        assertThat(result.getFirst().totalPrice()).isEqualByComparingTo("700.00");
    }

    private static Map<String, Object> component(String slot, Long skuId, String name, String price, String category) {
        return Map.of("slot_type", slot, "id", skuId, "name", name,
                "price", new BigDecimal(price), "category", category);
    }
}
