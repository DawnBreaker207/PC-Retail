package com.dawn.catalog.port.in;

import com.dawn.catalog.controller.request.SkuCreatedRequest;
import com.dawn.catalog.controller.request.SkuUpdatedRequest;
import com.dawn.catalog.dto.SkuDetailDto;

public interface CreateSkuUseCase {

    SkuDetailDto createSku(SkuCreatedRequest request);

    SkuDetailDto updateSku(Long id, SkuUpdatedRequest request);

    void softDeleteSku(Long id);
}
