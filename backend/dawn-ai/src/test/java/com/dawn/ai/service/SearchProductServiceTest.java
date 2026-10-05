package com.dawn.ai.service;

import com.dawn.ai.dto.AiSearchResult;
import com.dawn.ai.port.out.EmbeddingPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchProductServiceTest {

    @Mock
    EmbeddingPort embeddingPort;
    @Mock
    JdbcTemplate jdbcTemplate;

    @InjectMocks
    SearchProductService service;

    private Map<String, Object> row(long id) {
        return Map.of(
                "id", id,
                "name", "Laptop Test",
                "category_name", "Laptop",
                "brand_name", "Brand",
                "price", BigDecimal.valueOf(10000000),
                "thumbnail_url", "img");
    }

    @Test
    @DisplayName("AI chết (key sai/mất mạng): rớt về keyword, không 500")
    void embeddingFails_keywordFallback() {
        when(embeddingPort.searchSimilar(anyString(), anyInt()))
                .thenThrow(new RuntimeException("401 openrouter"));
        doReturn(List.of(new AiSearchResult(1L, "Laptop Test", "Laptop",
                "Brand", BigDecimal.valueOf(10000000), "img", 0.0)))
                .when(jdbcTemplate).query(anyString(), any(RowMapper.class), any(), any(), any());

        List<AiSearchResult> results = service.search("laptop gaming", 5);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).similarityScore()).isZero();
        verify(jdbcTemplate).query(contains("ILIKE"), any(RowMapper.class), any(), any(), any());
    }
}
