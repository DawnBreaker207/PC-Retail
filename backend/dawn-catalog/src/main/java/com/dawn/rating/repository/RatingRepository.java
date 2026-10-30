package com.dawn.rating.repository;

import com.dawn.rating.entity.Rating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    Page<Rating> findBySkuIdAndStatus(Long skuId, String status, Pageable pageable);

    List<Rating> findByUserIdAndSkuId(Long userId, Long skuId);

    boolean existsByUserIdAndSkuIdAndOrderId(Long userId, Long skuId, Long orderId);

    @Query("SELECT AVG(r.score) FROM Rating r WHERE r.skuId = :skuId AND r.status = 'APPROVED'")
    Double findAverageScoreBySkuId(@Param("skuId") Long skuId);

    @Query("SELECT COUNT(r) FROM Rating r WHERE r.skuId = :skuId AND r.status = 'APPROVED'")
    Long countApprovedBySkuId(@Param("skuId") Long skuId);

    @Query("SELECT r.score, COUNT(r) FROM Rating r WHERE r.skuId = :skuId AND r.status = 'APPROVED' GROUP BY r.score")
    List<Object[]> findScoreDistributionBySkuId(@Param("skuId") Long skuId);
}
