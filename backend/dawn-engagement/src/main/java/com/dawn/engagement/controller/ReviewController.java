package com.dawn.engagement.controller;

import com.dawn.common.security.AuthorizationConstants;
import com.dawn.engagement.controller.request.CreateReviewRequest;
import com.dawn.engagement.dto.ReviewDto;
import com.dawn.engagement.port.in.CreateReviewUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final CreateReviewUseCase createReviewUseCase;

    @PostMapping
    @PreAuthorize(AuthorizationConstants.CUSTOMER_ONLY)
    public ReviewDto create(@RequestBody CreateReviewRequest req) {
        return createReviewUseCase.createReview(req);
    }
}
