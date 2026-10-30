package com.dawn.engagement.infra.persistence;

import com.dawn.engagement.entity.Review;
import com.dawn.engagement.port.out.ReviewRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaReviewRepository extends JpaRepository<Review, Long>, ReviewRepository {
    @Override
    boolean existsBySubOrderIdAndSkuId(Long subOrderId, Long skuId);
}
