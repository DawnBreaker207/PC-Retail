package com.dawn.engagement.port.out;

import com.dawn.engagement.entity.Review;

public interface ReviewRepository {
    Review save(Review review);

    boolean existsBySubOrderIdAndSkuId(Long subOrderId, Long skuId);
}
