package com.dawn.catalog.service;

import com.dawn.catalog.dto.SkuCompareResponse;
import com.dawn.catalog.dto.SkuDetailDto;
import com.dawn.catalog.dto.SkuSummaryDto;
import com.dawn.catalog.entity.Sku;
import com.dawn.catalog.entity.enums.SkuStatus;
import com.dawn.catalog.port.in.QuerySkuUseCase;
import com.dawn.catalog.repository.SkuRepository;
import com.dawn.common.exception.wrapper.InvalidRequestException;
import com.dawn.common.exception.wrapper.ResourceNotFoundException;
import com.dawn.common.message.Message;
import com.dawn.common.response.ResponsePage;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SkuQueryService implements QuerySkuUseCase {

    private final SkuRepository skuRepository;

    @Override
    @Transactional(readOnly = true)
    public SkuDetailDto getById(Long id) {
        Sku sku = skuRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException(Message.format(Message.Catalog.SKU_NOT_FOUND, id)));
        return toDetail(sku);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponsePage<SkuSummaryDto> search(
            int page, int size,
            Long brandId, Long categoryId,
            BigDecimal minPrice, BigDecimal maxPrice,
            String searchTerm) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Specification<Sku> spec = buildSpec(brandId, categoryId, minPrice, maxPrice, searchTerm, false);
        Page<Sku> result = skuRepository.findAll(spec, pageable);

        return ResponsePage.of(result.map(this::toSummary));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SkuSummaryDto> searchKeyset(String keyword, Long lastId, int size) {
        int safeSize = Math.min(size, 100);
        Specification<Sku> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isNull(root.get("deletedAt")));
            predicates.add(cb.equal(root.get("status"), SkuStatus.ACTIVE));
            if (lastId != null) {
                predicates.add(cb.greaterThan(root.get("id"), lastId));
            }
            if (StringUtils.hasText(keyword)) {
                String escaped = keyword.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + escaped.toLowerCase() + "%", '\\'));
            }
            query.orderBy(cb.asc(root.get("id")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return skuRepository.findAll(spec, PageRequest.of(0, safeSize)).map(this::toSummary).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SkuCompareResponse> getSkusForCompare(List<Long> ids) {
        List<Sku> skus = skuRepository.findAllActiveByIds(ids);
        if (skus.isEmpty()) return List.of();

        long distinctCategories = skus.stream()
                .map(Sku::getCategoryId)
                .distinct()
                .count();

        if (distinctCategories > 1) {
            throw new InvalidRequestException(Message.Catalog.SKU_CATEGORY_MISMATCH);
        }

        return skus.stream().map(sku -> new SkuCompareResponse(
                        sku.getId(),
                        sku.getName(),
                        sku.getSlug(),
                        sku.getPrice(),
                        sku.getImageUrls() != null && !sku.getImageUrls().isEmpty()
                                ? sku.getImageUrls().getFirst()
                                : null,
                        sku.getSpecs()))
                .toList();
    }

    // --- Private helpers ---

    private Specification<Sku> buildSpec(
            Long brandId, Long categoryId,
            BigDecimal minPrice, BigDecimal maxPrice,
            String search, boolean includeNonActive) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isNull(root.get("deletedAt")));
            if (!includeNonActive) {
                predicates.add(cb.equal(root.get("status"), SkuStatus.ACTIVE));
            }
            if (brandId != null) {
                predicates.add(cb.equal(root.get("brandId"), brandId));
            }
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("categoryId"), categoryId));
            }
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            if (StringUtils.hasText(search)) {
                String escaped = search.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
                predicates.add(cb.like(
                        cb.lower(root.get("name")),
                        "%" + escaped.toLowerCase() + "%",
                        '\\'
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private SkuSummaryDto toSummary(Sku sku) {
        String primaryImage = sku.getImageUrls() != null && !sku.getImageUrls().isEmpty()
                ? sku.getImageUrls().get(0) : null;
        return new SkuSummaryDto(sku.getId(), sku.getSkuCode(), sku.getName(),
                sku.getSlug(), sku.getPrice(), sku.getStatus(),
                primaryImage, sku.getBrandId(), sku.getCategoryId());
    }

    private SkuDetailDto toDetail(Sku sku) {
        return new SkuDetailDto(sku.getId(), sku.getSkuCode(), sku.getName(),
                sku.getSlug(), sku.getDescription(), sku.getPrice(),
                sku.getStatus(), sku.getProductType(),
                sku.getAttributes(), sku.getSpecs(), sku.getImageUrls(),
                sku.getBrandId(), sku.getCategoryId(),
                null
        );
    }
}
