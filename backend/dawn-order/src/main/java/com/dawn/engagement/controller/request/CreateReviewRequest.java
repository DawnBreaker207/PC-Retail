package com.dawn.engagement.controller.request;

import java.util.List;

public record CreateReviewRequest(
        Long skuId,
        Long subOrderId,
        int rating,
        String comment,
        List<String> images) {
}
