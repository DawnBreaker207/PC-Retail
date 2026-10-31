package com.dawn.promotion.controller;

import com.dawn.common.response.ResponseObject;
import com.dawn.promotion.entity.FlashSaleCampaign;
import com.dawn.promotion.service.FlashSaleQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/promotion/flash-sales")
@RequiredArgsConstructor
public class FlashSaleController {

    private final FlashSaleQueryService flashSaleQueryService;

    @GetMapping("/active")
    public ResponseObject<List<FlashSaleCampaign>> active() {
        return ResponseObject.success(flashSaleQueryService.getActiveCampaigns());
    }
}
