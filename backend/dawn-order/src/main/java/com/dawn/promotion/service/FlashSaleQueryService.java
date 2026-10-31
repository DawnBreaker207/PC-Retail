package com.dawn.promotion.service;

import com.dawn.promotion.entity.FlashSaleCampaign;
import com.dawn.promotion.infra.persistence.JpaFlashSaleCampaignRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FlashSaleQueryService {

    private final JpaFlashSaleCampaignRepository campaignRepo;

    @Transactional(readOnly = true)
    public List<FlashSaleCampaign> getActiveCampaigns() {
        Instant now = Instant.now();
        return campaignRepo.findAll().stream()
                .filter(c -> c.getStatus().name().equals("ACTIVE")
                        && !c.getStartTime().isAfter(now) && c.getEndTime().isAfter(now))
                .toList();
    }
}
