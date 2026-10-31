package com.dawn.engagement.port.out;

import com.dawn.engagement.entity.Review;

import java.util.List;

public interface ReviewRepository {
    Review save(Review review);

    boolean existsBySubOrderIdAndSkuId(Long subOrderId, Long skuId);

    List<Review> findBySkuId(Long skuId);

    List<Review> findByUserId(Long userId);
}
