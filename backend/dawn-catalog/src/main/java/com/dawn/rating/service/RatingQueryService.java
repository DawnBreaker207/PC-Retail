package com.dawn.rating.service;

import com.dawn.common.response.ResponsePage;
import com.dawn.rating.dto.RatingDto;
import com.dawn.rating.dto.SkuRatingSummary;
import com.dawn.rating.entity.Rating;
import com.dawn.rating.entity.enums.RatingStatus;
import com.dawn.rating.repository.RatingReadRepository;
import com.dawn.rating.repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RatingQueryService {

    private final RatingRepository ratingRepository;
    private final RatingReadRepository ratingReadRepository;

    @Transactional(readOnly = true)
    public ResponsePage<RatingDto> getSkuRatings(Long skuId, Pageable pageable) {
        Page<Rating> page = ratingRepository.findBySkuIdAndStatus(skuId, RatingStatus.APPROVED.name(), pageable);
        return ResponsePage.of(page.map(this::toDto));
    }

    @Transactional(readOnly = true)
    public SkuRatingSummary getSkuSummary(Long skuId) {
        Double avg = ratingReadRepository.findAverageScoreBySkuId(skuId);
        Long count = ratingReadRepository.countApprovedBySkuId(skuId);
        List<Object[]> raw = ratingReadRepository.findScoreDistributionBySkuId(skuId);

        Map<Integer, Long> distribution = new HashMap<>();
        for (Object[] row : raw) {
            distribution.put(((Number) row[0]).intValue(), ((Number) row[1]).longValue());
        }

        return new SkuRatingSummary(skuId, avg != null ? avg : 0.0, count != null ? count : 0L, distribution);
    }

    private RatingDto toDto(Rating r) {
        return new RatingDto(
                r.getId(), r.getUserId(), r.getSkuId(),
                r.getScore(), r.getComment(), r.getImageUrls(),
                r.getStatus() != null ? r.getStatus().name() : null,
                r.getCreatedAt()
        );
    }
}
