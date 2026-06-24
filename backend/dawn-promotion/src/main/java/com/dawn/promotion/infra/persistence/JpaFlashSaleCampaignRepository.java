package com.dawn.promotion.infra.persistence;

import com.dawn.promotion.entity.FlashSaleCampaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaFlashSaleCampaignRepository extends JpaRepository<FlashSaleCampaign, Long> {
}
