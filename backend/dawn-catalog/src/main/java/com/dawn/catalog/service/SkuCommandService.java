package com.dawn.catalog.service;

import com.dawn.catalog.controller.request.SkuCreatedRequest;
import com.dawn.catalog.controller.request.SkuUpdatedRequest;
import com.dawn.catalog.dto.SkuDetailDto;
import com.dawn.catalog.entity.Sku;
import com.dawn.catalog.entity.enums.ProductType;
import com.dawn.catalog.entity.enums.SkuStatus;
import com.dawn.catalog.port.in.CreateSkuUseCase;
import com.dawn.catalog.repository.BrandRepository;
import com.dawn.catalog.repository.CategoryRepository;
import com.dawn.catalog.repository.SkuRepository;
import com.dawn.common.auditlog.LogConstant;
import com.dawn.common.auditlog.Loggable;
import com.dawn.common.contract.OrderQueryPort;
import com.dawn.common.exception.wrapper.ConflictException;
import com.dawn.common.exception.wrapper.ResourceNotFoundException;
import com.dawn.common.message.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class SkuCommandService implements CreateSkuUseCase {

    private final SkuRepository skuRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final OrderQueryPort orderQueryPort;

    @Override
    @Transactional
    @Loggable(action = LogConstant.Action.CREATE, entity = LogConstant.Entity.PRODUCT)
    public SkuDetailDto createSku(SkuCreatedRequest request) {
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

    @Override
    @Transactional
    @Loggable(action = LogConstant.Action.UPDATE, entity = LogConstant.Entity.PRODUCT)
    public SkuDetailDto updateSku(Long id, SkuUpdatedRequest request) {
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

    @Override
    @Transactional
    @Loggable(action = LogConstant.Action.DELETE, entity = LogConstant.Entity.PRODUCT)
    public void softDeleteSku(Long id) {
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

    // --- Private helpers ---

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
