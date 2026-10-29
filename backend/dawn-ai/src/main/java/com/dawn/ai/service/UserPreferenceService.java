package com.dawn.ai.service;

import com.dawn.ai.entity.UserPreference;
import com.dawn.ai.infra.persistence.JpaUserPreferenceRepository;
import com.dawn.common.event.ClickstreamEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserPreferenceService {

    private final JpaUserPreferenceRepository repository;

    @Transactional
    public void record(ClickstreamEvent event) {
        if (event.userId() == null) {
            return;
        }
        UserPreference preference = repository.findById(event.userId())
                .orElseGet(() -> UserPreference.builder()
                        .userId(event.userId())
                        .preferenceVector(new HashMap<>())
                        .averagePrice(BigDecimal.ZERO)
                        .eventCount(0)
                        .updatedAt(Instant.now())
                        .build());

        Map<String, Double> vector = new HashMap<>(
                preference.getPreferenceVector() == null ? Map.of() : preference.getPreferenceVector());
        if (event.componentCategory() != null && !event.componentCategory().isBlank()) {
            vector.merge(event.componentCategory(), 1.0, Double::sum);
        }
        preference.setPreferenceVector(vector);

        if (event.price() != null) {
            BigDecimal count = BigDecimal.valueOf(preference.getEventCount());
            BigDecimal previous = preference.getAveragePrice() == null ? BigDecimal.ZERO : preference.getAveragePrice();
            preference.setAveragePrice(previous.multiply(count).add(event.price())
                    .divide(count.add(BigDecimal.ONE), 2, RoundingMode.HALF_UP));
        }
        preference.setEventCount(preference.getEventCount() + 1);
        preference.setUpdatedAt(Instant.now());
        repository.save(preference);
    }

    @Transactional(readOnly = true)
    public Map<String, Double> weights(Long userId) {
        return repository.findById(userId)
                .map(UserPreference::getPreferenceVector)
                .orElse(Map.of());
    }
}
