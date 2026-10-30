package com.dawn.engagement.port.in;

import com.dawn.engagement.dto.ReviewDto;
import com.dawn.engagement.controller.request.CreateReviewRequest;

public interface CreateReviewUseCase {
    ReviewDto createReview(CreateReviewRequest req);
}
