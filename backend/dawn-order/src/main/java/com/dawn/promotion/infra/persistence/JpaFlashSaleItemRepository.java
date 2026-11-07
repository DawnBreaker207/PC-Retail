package com.dawn.promotion.infra.persistence;

import com.dawn.promotion.entity.FlashSaleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaFlashSaleItemRepository extends JpaRepository<FlashSaleItem, Long> {
    List<FlashSaleItem> findByCampaignId(Long campaignId);
    void deleteByCampaignId(Long campaignId);
}
