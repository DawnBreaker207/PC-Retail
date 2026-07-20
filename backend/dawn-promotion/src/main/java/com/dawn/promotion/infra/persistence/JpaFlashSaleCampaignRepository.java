package com.dawn.promotion.infra.persistence;

import com.dawn.promotion.entity.FlashSaleCampaign;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Repository
public interface JpaFlashSaleCampaignRepository extends JpaRepository<FlashSaleCampaign, Long> {

    @Query("""
            UPDATE FlashSaleCampaign c SET c.status = 'ACTIVE'
            WHERE c.status = 'UPCOMING' AND c.startTime <= :now AND c.endTime > :now
            """)
    @Modifying
    @Transactional
    int activeCampaigns(@Param("now") Instant now);

    @Query("""
            UPDATE FlashSaleCampaign c SET c.status = 'ENDED'
            WHERE c.status = 'ACTIVE' AND c.startTime <= :now AND c.endTime <= :now
            """)
    @Modifying
    @Transactional
    int endCampaigns(@Param("now") Instant now);
}
