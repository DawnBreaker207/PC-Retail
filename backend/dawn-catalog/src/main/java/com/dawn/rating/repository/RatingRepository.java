package com.dawn.rating.repository;

import com.dawn.rating.entity.Rating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    Page<Rating> findBySkuIdAndStatus(Long skuId, String status, Pageable pageable);

    List<Rating> findByUserIdAndSkuId(Long userId, Long skuId);

    boolean existsByUserIdAndSkuIdAndOrderId(Long userId, Long skuId, Long orderId);
}
