package com.dawn.catalog.service;

import com.dawn.catalog.controller.request.SkuCreatedRequest;
import com.dawn.catalog.controller.request.SkuUpdatedRequest;
import com.dawn.catalog.dto.SkuCompareResponse;
import com.dawn.catalog.dto.SkuDetailDto;
import com.dawn.catalog.dto.SkuSummaryDto;
import com.dawn.catalog.entity.Sku;
import com.dawn.common.auditlog.LogConstant;
import com.dawn.common.auditlog.Loggable;
import com.dawn.catalog.entity.enums.ProductType;
import com.dawn.catalog.entity.enums.SkuStatus;
import com.dawn.catalog.repository.BrandRepository;
import com.dawn.catalog.repository.CategoryRepository;
import com.dawn.catalog.repository.SkuRepository;
import com.dawn.common.exception.wrapper.ConflictException;
import com.dawn.common.exception.wrapper.InvalidRequestException;
import com.dawn.common.exception.wrapper.ResourceNotFoundException;
import com.dawn.common.message.Message;
import com.dawn.common.response.ResponsePage;
import com.dawn.common.contract.OrderQueryPort;
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
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SkuService {
    private final SkuRepository skuRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final OrderQueryPort orderQueryPort;

    @Transactional(readOnly = true)
    public ResponsePage<SkuSummaryDto> search(
            int page, int size,
            Long brandId, Long categoryId,
            BigDecimal minPrice, BigDecimal maxPrice,
            String search) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Specification<Sku> spec = buildSpec(brandId, categoryId, minPrice, maxPrice, search, false);
        Page<Sku> result = skuRepository.findAll(spec, pageable);

        return ResponsePage.of(result.map(this::toSummary));
    }

    @Transactional(readOnly = true)
    public SkuDetailDto getDetail(Long id) {
        Sku sku = skuRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException(Message.format(Message.Catalog.SKU_NOT_FOUND, id)));
        return toDetail(sku);
    }

    @Transactional
    @Loggable(action = LogConstant.Action.CREATE, entity = LogConstant.Entity.PRODUCT)
    public SkuDetailDto create(SkuCreatedRequest request) {
        if (skuRepository.existsBySkuCodeAndDeletedAtIsNull(request.skuCode())) {
            throw new ConflictException(Message.Catalog.SKU_CODE_ALREADY_EXISTS);
        }
        categoryRepository.findByIdAndDeletedAtIsNull(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException(Message.Catalog.CATEGORY_NOT_FOUND));
        if (request.brandId() != null) {
            brandRepository.findByIdAndDeletedAtIsNull(request.brandId())
                    .orElseThrow(() -> new ResourceNotFoundException(Message.Catalog.BRAND_NOT_FOUND));
        }

        String slug = generateSlug(request.name());

        Sku sku = Sku.builder()
                .skuCode(request.skuCode())
                .name(request.name())
                .slug(slug)
                .description(request.description())
                .brandId(request.brandId())
                .categoryId(request.categoryId())
                .price(request.price())
                .productType(request.productType() != null ? request.productType() : ProductType.PHYSICAL)
                .status(SkuStatus.ACTIVE)
                .attributes(request.attributes() != null ? request.attributes() : new HashMap<>())
                .specs(request.specs() != null ? request.specs() : new HashMap<>())
                .imageUrls(request.imageUrls() != null ? request.imageUrls() : new ArrayList<>())
                .build();

        skuRepository.save(sku);
        return toDetail(sku);
    }

    @Transactional
    @Loggable(action = LogConstant.Action.UPDATE, entity = LogConstant.Entity.PRODUCT)
    public SkuDetailDto update(Long id, SkuUpdatedRequest request) {
        Sku sku = skuRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException(Message.format(Message.Catalog.SKU_NOT_FOUND, id)));

        if (request.name() != null) sku.setName(request.name());
        if (request.name() != null) {
            sku.setSlug(generateSlug(request.name()));
        }
        if (request.description() != null) sku.setDescription(request.description());
        if (request.price() != null) sku.setPrice(request.price());
        if (request.status() != null) sku.setStatus(request.status());
        if (request.attributes() != null) sku.setAttributes(request.attributes());
        if (request.specs() != null) sku.setSpecs(request.specs());
        if (request.imageUrls() != null) sku.setImageUrls(request.imageUrls());

        skuRepository.save(sku);
        return toDetail(sku);
    }

    @Transactional
    @Loggable(action = LogConstant.Action.DELETE, entity = LogConstant.Entity.PRODUCT)
    public void softDelete(Long id) {
        Sku sku = skuRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException(Message.format(Message.Catalog.SKU_NOT_FOUND, id)));
        if (orderQueryPort.existsOrderItemBySkuId(id)) {
            sku.setDeletedAt(Instant.now());
        } else {
            skuRepository.deleteById(id);
            return;
        }
        skuRepository.save(sku);
    }

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
                        sku.getImageUrls() != null
                                && !sku.getImageUrls().isEmpty()
                                ? sku.getImageUrls().getFirst()
                                : null,
                        sku.getSpecs()))
                .toList();
    }

    // Private helpers

    private Specification<Sku> buildSpec(
            Long brandId, Long categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String search,
            boolean includeNonActive) {
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

    private String generateSlug(String name) {
        String base = name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");
        String slug = base;
        int counter = 2;
        while (skuRepository.existsBySlugAndDeletedAtIsNull(slug)) {
            slug = base + "-" + counter++;
        }
        return slug;
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
