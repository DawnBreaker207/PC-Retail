package com.dawn.promotion.controller;

import com.dawn.common.response.ResponseObject;
import com.dawn.promotion.entity.FlashSaleCampaign;
import com.dawn.promotion.infra.persistence.JpaFlashSaleCampaignRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/promotion/flash-sales")
@RequiredArgsConstructor
public class FlashSaleController {

    private final JpaFlashSaleCampaignRepository campaignRepo;

    @GetMapping("/active")
    public ResponseObject<List<FlashSaleCampaign>> active() {
        Instant now = Instant.now();
        return ResponseObject.success(campaignRepo.findAll().stream()
                .filter(c -> c.getStatus().name().equals("ACTIVE")
                        && !c.getStartTime().isAfter(now) && c.getEndTime().isAfter(now))
                .toList());
    }
}
