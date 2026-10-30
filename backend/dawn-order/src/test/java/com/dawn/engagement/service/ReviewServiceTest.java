package com.dawn.engagement.service;

import com.dawn.common.exception.wrapper.ConflictException;
import com.dawn.common.exception.wrapper.InvalidRequestException;
import com.dawn.engagement.controller.request.CreateReviewRequest;
import com.dawn.engagement.entity.Review;
import com.dawn.engagement.port.out.ReviewRepository;
import com.dawn.common.contract.OrderQueryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    ReviewRepository reviewRepository;
    @Mock
    OrderQueryPort orderQueryPort;

    ReviewService service;

    @BeforeEach
    void setUp() {
        service = new ReviewService(reviewRepository, orderQueryPort);
    }

    private CreateReviewRequest req(int rating) {
        return new CreateReviewRequest(10L, 5L, rating, "ok", List.of());
    }

    @Test
    @DisplayName("Chưa giao: 400 chứ không phải 404")
    void notDelivered_badRequest() {
        when(orderQueryPort.isSubOrderDelivered(5L)).thenReturn(false);

        assertThatThrownBy(() -> service.createReview(req(5)))
                .isInstanceOf(InvalidRequestException.class);

        verifyNoInteractions(reviewRepository);
    }

    @Test
    @DisplayName("Rating ngoài 1-5: 400")
    void ratingOutOfRange_badRequest() {
        when(orderQueryPort.isSubOrderDelivered(5L)).thenReturn(true);

        assertThatThrownBy(() -> service.createReview(req(6)))
                .isInstanceOf(InvalidRequestException.class);
        assertThatThrownBy(() -> service.createReview(req(0)))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    @DisplayName("Đã review rồi: 409, không save lần 2")
    void duplicate_conflict() {
        when(orderQueryPort.isSubOrderDelivered(5L)).thenReturn(true);
        when(reviewRepository.existsBySubOrderIdAndSkuId(5L, 10L)).thenReturn(true);

        assertThatThrownBy(() -> service.createReview(req(5)))
                .isInstanceOf(ConflictException.class);

        verify(reviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("Hợp lệ: save 1 lần")
    void happyPath_saves() {
        when(orderQueryPort.isSubOrderDelivered(5L)).thenReturn(true);
        when(reviewRepository.existsBySubOrderIdAndSkuId(5L, 10L)).thenReturn(false);
        when(reviewRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.createReview(req(5));

        verify(reviewRepository, times(1)).save(any(Review.class));
    }
}
