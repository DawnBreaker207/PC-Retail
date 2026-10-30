package com.dawn.engagement.service;

import com.dawn.common.exception.wrapper.InvalidRequestException;
import com.dawn.common.exception.wrapper.ConflictException;
import com.dawn.common.exception.wrapper.ResourceNotFoundException;
import com.dawn.common.message.Message;
import com.dawn.common.utils.SecurityUtils;
import com.dawn.engagement.controller.request.CreateReviewRequest;
import com.dawn.engagement.dto.ReviewDto;
import com.dawn.engagement.entity.Review;
import com.dawn.engagement.port.in.CreateReviewUseCase;
import com.dawn.engagement.port.out.ReviewRepository;
import com.dawn.common.contract.OrderQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService implements CreateReviewUseCase {
    private final ReviewRepository reviewRepository;
    private final OrderQueryPort orderQueryPort;

    @Transactional
    @Override
    public ReviewDto createReview(CreateReviewRequest req) {
        Long userId = SecurityUtils.getCurrentUserId();

        if (!orderQueryPort.isSubOrderDelivered(req.subOrderId())) {
            throw new InvalidRequestException(Message.Engagement.REVIEW_ONLY_AFTER_DELIVERED);
        }

        if (req.rating() < 1 || req.rating() > 5) {
            throw new InvalidRequestException(Message.Engagement.RATING_OUT_OF_RANGE);
        }

        if (reviewRepository.existsBySubOrderIdAndSkuId(req.subOrderId(), req.skuId())) {
            throw new ConflictException(Message.Engagement.REVIEW_ALREADY_EXISTS);
        }

        Review review = Review.builder()
                .subOrderId(req.subOrderId())
                .skuId(req.skuId())
                .userId(userId)
                .rating(req.rating())
                .comment(req.comment())
                .images(req.images())
                .build();
        review = reviewRepository.save(review);
        return toDto(review);
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
