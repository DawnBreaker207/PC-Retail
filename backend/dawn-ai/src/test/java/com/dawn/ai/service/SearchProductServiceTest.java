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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchProductServiceTest {

    @Mock EmbeddingPort embeddingPort;
    @Mock JdbcTemplate jdbcTemplate;

    @InjectMocks SearchProductService service;

    private AiSearchResult sampleResult() {
        return new AiSearchResult(1L, "Laptop Test", "Laptop", "Brand",
                BigDecimal.valueOf(10_000_000), "img", 0.5);
    }

    @Test
    @DisplayName("FTS thành công: trả kết quả đã sort theo rank")
    void ftsSearch_returnsRankedResults() {
        doReturn(List.of(sampleResult()))
                .when(jdbcTemplate).query(contains("ts_rank_cd"), any(RowMapper.class),
                        any(), any(), any());

        List<AiSearchResult> results = service.search("laptop gaming", 5);

        assertThat(results).hasSize(1);
    }

    @Test
    @DisplayName("FTS không ra kết quả: rớt về ILIKE keyword search")
    void ftsReturnsEmpty_keywordFallback() {
        // FTS returns empty
        doReturn(List.<AiSearchResult>of())
                .when(jdbcTemplate).query(contains("ts_rank_cd"), any(RowMapper.class),
                        any(), any(), any());
        // Keyword search returns a result
        doReturn(List.of(sampleResult()))
                .when(jdbcTemplate).query(contains("ILIKE"), any(RowMapper.class),
                        any(), any(), any());

        List<AiSearchResult> results = service.search("laptop gaming", 5);

        assertThat(results).hasSize(1);
        verify(jdbcTemplate).query(contains("ILIKE"), any(RowMapper.class), any(), any(), any());
    }

    @Test
    @DisplayName("Query trống: trả danh sách rỗng ngay lập tức")
    void emptyQuery_returnsEmpty() {
        List<AiSearchResult> results = service.search("  ", 5);
        assertThat(results).isEmpty();
        verifyNoInteractions(jdbcTemplate);
    }
}
