package com.dawn.rating.repository;

import com.dawn.rating.entity.Rating;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Read-optimised repository holding aggregate queries for the rating read model.
 * Used exclusively by {@link com.dawn.rating.service.RatingQueryService}.
 */
public interface RatingReadRepository extends Repository<Rating, Long> {

    @Query("SELECT AVG(r.score) FROM Rating r WHERE r.skuId = :skuId AND r.status = 'APPROVED'")
    Double findAverageScoreBySkuId(@Param("skuId") Long skuId);

    @Query("SELECT COUNT(r) FROM Rating r WHERE r.skuId = :skuId AND r.status = 'APPROVED'")
    Long countApprovedBySkuId(@Param("skuId") Long skuId);

    @Query("SELECT r.score, COUNT(r) FROM Rating r WHERE r.skuId = :skuId AND r.status = 'APPROVED' GROUP BY r.score")
    List<Object[]> findScoreDistributionBySkuId(@Param("skuId") Long skuId);
}
