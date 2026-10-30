package com.dawn.reporting.service;

import com.dawn.common.exception.wrapper.ConflictException;
import com.dawn.common.exception.wrapper.InvalidRequestException;
import com.dawn.reporting.entity.StockTakeSession;
import com.dawn.reporting.infra.persistence.JpaStockTakeItemRepository;
import com.dawn.reporting.infra.persistence.JpaStockTakeSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockTakeServiceTest {

    @Mock
    JpaStockTakeSessionRepository sessionRepo;
    @Mock
    JpaStockTakeItemRepository itemRepo;
    @Mock
    JdbcTemplate jdbc;

    StockTakeService service;

    @BeforeEach
    void setUp() {
        service = new StockTakeService(sessionRepo, itemRepo, jdbc);
        lenient().when(sessionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    @DisplayName("Danh sách rỗng: 400")
    void emptyCounts_badRequest() {
        assertThatThrownBy(() -> service.start(9L, List.of()))
                .isInstanceOf(InvalidRequestException.class);

        verifyNoInteractions(sessionRepo);
    }

    @Test
    @DisplayName("Số đếm âm: 400")
    void negativeCount_badRequest() {
        assertThatThrownBy(() -> service.start(9L, List.of(Map.of("skuId", 1, "countedQuantity", -1))))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    @DisplayName("Chốt session đã COMPLETED: 409")
    void completeTwice_conflict() {
        StockTakeSession done = StockTakeSession.builder().status("COMPLETED").build();
        when(sessionRepo.findById(3L)).thenReturn(Optional.of(done));

        assertThatThrownBy(() -> service.complete(3L))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("Chốt session DRAFT: chuyển COMPLETED + ghi giờ")
    void completeDraft_completes() {
        StockTakeSession draft = StockTakeSession.builder().status("IN_PROGRESS").build();
        when(sessionRepo.findById(3L)).thenReturn(Optional.of(draft));

        StockTakeSession out = service.complete(3L);

        assertThat(out.getStatus()).isEqualTo("COMPLETED");
        assertThat(out.getCompletedAt()).isNotNull();
    }
}
