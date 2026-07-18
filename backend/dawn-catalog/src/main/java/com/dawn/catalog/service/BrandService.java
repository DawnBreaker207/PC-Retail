package com.dawn.catalog.service;

import com.dawn.catalog.controller.request.BrandRequest;
import com.dawn.catalog.dto.BrandDto;
import com.dawn.catalog.entity.Brand;
import com.dawn.catalog.repository.BrandRepository;
import com.dawn.catalog.repository.SkuRepository;
import com.dawn.common.exception.wrapper.InvalidRequestException;
import com.dawn.common.exception.wrapper.ConflictException;
import com.dawn.common.exception.wrapper.ResourceNotFoundException;
import com.dawn.common.message.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BrandService {
    private final BrandRepository brandRepository;
    private final SkuRepository skuRepository;

    @Transactional(readOnly = true)
    public List<BrandDto> getAll() {
        return brandRepository.findAllByDeletedAtIsNull()
                .stream()
                .sorted(Comparator.comparing(Brand::getName))
                .map(b -> new BrandDto(b.getId(), b.getName(), b.getLogoUrl()))
                .toList();
    }

    @Transactional
    public BrandDto create(BrandRequest req) {
        if (brandRepository.existsByNameAndDeletedAtIsNull(req.name())) {
            throw new ConflictException(Message.Catalog.BRAND_ALREADY_EXISTS);
        }

        Brand brand = Brand.builder()
                .name(req.name())
                .logoUrl(req.logoUrl())
                .build();
        brandRepository.save(brand);
        return new BrandDto(brand.getId(), brand.getName(), brand.getLogoUrl());
    }

    @Transactional
    public BrandDto update(Long id, BrandRequest req) {
        Brand brand = brandRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException(Message.format(Message.Catalog.BRAND_NOT_FOUND, id)));
        brand.setName(req.name());
        brand.setLogoUrl(req.logoUrl());
        brandRepository.save(brand);
        return new BrandDto(brand.getId(), brand.getName(), brand.getLogoUrl());
    }

    @Transactional
    public void delete(Long id) {
        Brand brand = brandRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException(Message.format(Message.Catalog.BRAND_NOT_FOUND, id)));
        if (skuRepository.existsByBrandIdAndDeletedAtIsNull(id)) {
            throw new InvalidRequestException(Message.Catalog.BRAND_HAS_SKU);
        }

        brand.setDeletedAt(Instant.now());
        brandRepository.save(brand);
    }
}
