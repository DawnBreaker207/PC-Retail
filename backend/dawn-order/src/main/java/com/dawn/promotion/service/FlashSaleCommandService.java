package com.dawn.promotion.service;

import com.dawn.common.exception.wrapper.ConflictException;
import com.dawn.common.exception.wrapper.InvalidRequestException;
import com.dawn.common.exception.wrapper.ResourceNotFoundException;
import com.dawn.promotion.entity.FlashSaleCampaign;
import com.dawn.promotion.entity.FlashSaleItem;
import com.dawn.promotion.entity.enums.FlashSaleStatus;
import com.dawn.promotion.infra.persistence.JpaFlashSaleCampaignRepository;
import com.dawn.promotion.infra.persistence.JpaFlashSaleItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FlashSaleCommandService {

    private final JpaFlashSaleCampaignRepository campaignRepo;
    private final JpaFlashSaleItemRepository itemRepo;

    @Transactional
    public FlashSaleCampaign createCampaign(String name, Instant startTime, Instant endTime) {
        if (!startTime.isBefore(endTime)) {
            throw new InvalidRequestException("startTime must be before endTime");
        }
        return campaignRepo.save(FlashSaleCampaign.builder()
                .name(name)
                .startTime(startTime)
                .endTime(endTime)
                .status(FlashSaleStatus.UPCOMING)
                .build());
    }

    @Transactional
    public FlashSaleItem addItem(Long campaignId, Long skuId, BigDecimal flashPrice, int quantityLimit) {
        FlashSaleCampaign campaign = campaignRepo.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found: " + campaignId));
        if (campaign.getStatus() != FlashSaleStatus.UPCOMING) {
            throw new ConflictException("Cannot modify a campaign that is " + campaign.getStatus());
        }
        return itemRepo.save(FlashSaleItem.builder()
                .campaignId(campaignId)
                .skuId(skuId)
                .flashPrice(flashPrice)
                .quantityLimit(quantityLimit)
                .build());
    }

    @Transactional
    public void removeItem(Long itemId) {
        itemRepo.deleteById(itemId);
    }

    @Transactional
    public void deleteCampaign(Long campaignId) {
        FlashSaleCampaign campaign = campaignRepo.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found: " + campaignId));
        if (campaign.getStatus() != FlashSaleStatus.UPCOMING) {
            throw new ConflictException("Only UPCOMING campaigns can be deleted");
        }
        itemRepo.deleteByCampaignId(campaignId);
        campaignRepo.deleteById(campaignId);
    }

    public List<FlashSaleItem> getItems(Long campaignId) {
        return itemRepo.findByCampaignId(campaignId);
    }
}
