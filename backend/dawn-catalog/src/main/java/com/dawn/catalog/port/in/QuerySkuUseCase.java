package com.dawn.catalog.port.in;

import com.dawn.catalog.dto.SkuCompareResponse;
import com.dawn.catalog.dto.SkuDetailDto;
import com.dawn.catalog.dto.SkuSummaryDto;
import com.dawn.common.response.ResponsePage;

import java.math.BigDecimal;
import java.util.List;

public interface QuerySkuUseCase {

    SkuDetailDto getById(Long id);

    ResponsePage<SkuSummaryDto> search(int page, int size,
                                       Long brandId, Long categoryId,
                                       BigDecimal minPrice, BigDecimal maxPrice,
                                       String searchTerm);

    List<SkuSummaryDto> searchKeyset(String keyword, Long lastId, int size);

    List<SkuCompareResponse> getSkusForCompare(List<Long> ids);
}
