package com.dawn.promotion.service;

import com.dawn.promotion.infra.persistence.JpaFlashSaleCampaignRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FlashSaleCommandService {

    private final JpaFlashSaleCampaignRepository campaignRepo;

    // Future write operations go here
    @Transactional
    public void placeholder() {
        // Placeholder for future command methods
    }
}
