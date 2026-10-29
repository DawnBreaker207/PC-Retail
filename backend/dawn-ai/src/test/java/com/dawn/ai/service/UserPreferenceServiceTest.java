package com.dawn.ai.service;

import com.dawn.ai.entity.UserPreference;
import com.dawn.ai.infra.persistence.JpaUserPreferenceRepository;
import com.dawn.common.event.ClickstreamEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserPreferenceServiceTest {

    @Mock
    JpaUserPreferenceRepository repository;

    @Test
    @DisplayName("record: cộng dồn trọng số danh mục + giá trung bình")
    void record_accumulatesPreference() {
        UserPreference existing = UserPreference.builder()
                .userId(1L)
                .preferenceVector(Map.of("CPU", 2.0))
                .averagePrice(new BigDecimal("100.00"))
                .eventCount(1)
                .build();
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        UserPreferenceService service = new UserPreferenceService(repository);
        service.record(new ClickstreamEvent(1L, 10L, "CPU", new BigDecimal("200.00"), Map.of()));

        ArgumentCaptor<UserPreference> captor = ArgumentCaptor.forClass(UserPreference.class);
        verify(repository).save(captor.capture());
        UserPreference saved = captor.getValue();
        assertThat(saved.getPreferenceVector()).containsEntry("CPU", 3.0);
        assertThat(saved.getAveragePrice()).isEqualByComparingTo("150.00");
        assertThat(saved.getEventCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("record: guest (userId null) bỏ qua")
    void record_guestSkipped() {
        new UserPreferenceService(repository)
                .record(new ClickstreamEvent(null, 10L, "CPU", BigDecimal.TEN, Map.of()));

        verify(repository, never()).save(any());
    }
}
