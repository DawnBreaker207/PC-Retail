package com.dawn.rating.service;

import com.dawn.common.exception.wrapper.ConflictException;
import com.dawn.common.exception.wrapper.PermissionDeniedException;
import com.dawn.common.exception.wrapper.ResourceNotFoundException;
import com.dawn.common.outbox.OutboxPublisher;
import com.dawn.rating.dto.RatingDto;
import com.dawn.rating.dto.RatingRequest;
import com.dawn.rating.entity.Rating;
import com.dawn.rating.entity.enums.RatingStatus;
import com.dawn.rating.infra.remote.OrderVerifyFeignClient;
import com.dawn.rating.repository.RatingRepository;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RatingCommandService {

    private final RatingRepository ratingRepository;
    private final OrderVerifyFeignClient orderVerifyFeignClient;
    private final OutboxPublisher outboxPublisher;

    @Transactional
    @Bulkhead(name = "order")
    public RatingDto submitRating(Long userId, RatingRequest req) {
        if (ratingRepository.existsByUserIdAndSkuIdAndOrderId(userId, req.skuId(), req.orderId())) {
            throw new ConflictException("You have already rated this product for the given order.");
        }

        Map<String, Boolean> verify = orderVerifyFeignClient.verifyPurchase(userId, req.skuId());
        if (!Boolean.TRUE.equals(verify.get("purchased"))) {
            throw new PermissionDeniedException("You must have purchased this product to leave a review.");
        }

        Rating rating = Rating.builder()
                .userId(userId)
                .skuId(req.skuId())
                .orderId(req.orderId())
                .score(req.score())
                .comment(req.comment())
                .imageUrls(req.imageUrls())
                .status(RatingStatus.PENDING_REVIEW)
                .build();

        ratingRepository.save(rating);

        outboxPublisher.publish("rating-events", String.valueOf(req.skuId()), Map.of(
                "type", "RATING_SUBMITTED",
                "ratingId", rating.getId(),
                "skuId", req.skuId(),
                "score", req.score()
        ));

        log.info("Rating {} submitted for skuId={} by userId={}", rating.getId(), req.skuId(), userId);
        return toDto(rating);
    }

    @Transactional
    public void approveRating(Long ratingId) {
        Rating rating = findOrThrow(ratingId);
        rating.setStatus(RatingStatus.APPROVED);
        ratingRepository.save(rating);

        outboxPublisher.publish("rating-events", String.valueOf(rating.getSkuId()), Map.of(
                "type", "RATING_APPROVED",
                "ratingId", ratingId,
                "skuId", rating.getSkuId(),
                "score", rating.getScore()
        ));
    }

    @Transactional
    public void rejectRating(Long ratingId, String reason) {
        Rating rating = findOrThrow(ratingId);
        rating.setStatus(RatingStatus.REJECTED);
        ratingRepository.save(rating);
        log.info("Rating {} rejected: {}", ratingId, reason);
    }

    private Rating findOrThrow(Long id) {
        return ratingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rating not found: " + id));
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
