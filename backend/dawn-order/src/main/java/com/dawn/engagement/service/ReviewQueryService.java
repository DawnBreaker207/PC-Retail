package com.dawn.engagement.service;

import com.dawn.engagement.dto.ReviewDto;
import com.dawn.engagement.entity.Review;
import com.dawn.engagement.port.out.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewQueryService {

    private final ReviewRepository reviewRepository;

    @Transactional(readOnly = true)
    public List<ReviewDto> listBySkuId(Long skuId) {
        return reviewRepository.findAll().stream()
                .filter(r -> r.getSkuId().equals(skuId))
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReviewDto> listByUserId(Long userId) {
        return reviewRepository.findAll().stream()
                .filter(r -> r.getUserId().equals(userId))
                .map(this::toDto)
                .toList();
    }

    private ReviewDto toDto(Review review) {
        return new ReviewDto(
                review.getId(),
                review.getSubOrderId(),
                review.getSkuId(),
                review.getUserId(),
                review.getRating(),
                review.getComment(),
                review.getImages());
    }
}
