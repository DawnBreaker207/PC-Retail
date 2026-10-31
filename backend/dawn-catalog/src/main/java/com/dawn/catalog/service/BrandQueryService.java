package com.dawn.catalog.service;

import com.dawn.catalog.dto.BrandDto;
import com.dawn.catalog.entity.Brand;
import com.dawn.catalog.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BrandQueryService {

    private final BrandRepository brandRepository;

    @Transactional(readOnly = true)
    public List<BrandDto> getAll() {
        return brandRepository.findAllByDeletedAtIsNull()
                .stream()
                .sorted(Comparator.comparing(Brand::getName))
                .map(b -> new BrandDto(b.getId(), b.getName(), b.getLogoUrl()))
                .toList();
    }
}
