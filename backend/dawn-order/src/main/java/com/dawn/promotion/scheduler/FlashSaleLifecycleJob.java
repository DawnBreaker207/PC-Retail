package com.dawn.promotion.scheduler;

import com.dawn.promotion.infra.persistence.JpaFlashSaleCampaignRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class FlashSaleLifecycleJob {
    private final JpaFlashSaleCampaignRepository campaignRepository;

    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void processCampaignLifecycle() {
        int activated = campaignRepository.activeCampaigns(Instant.now());
        int ended = campaignRepository.endCampaigns(Instant.now());
        if (activated > 0 || ended > 0) {
            log.info("FlashSale lifecycle: {} activated, {} ended", activated, ended);
        }
    }
}
