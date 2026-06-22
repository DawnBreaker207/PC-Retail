package com.dawn.catalog.service;

import com.dawn.catalog.controller.request.CategoryRequest;
import com.dawn.catalog.dto.CategoryDto;
import com.dawn.catalog.entity.Category;
import com.dawn.catalog.repository.CategoryRepository;
import com.dawn.catalog.repository.SkuRepository;
import com.dawn.common.exception.wrapper.InvalidRequestException;
import com.dawn.common.exception.wrapper.ResourceAlreadyExistedException;
import com.dawn.common.exception.wrapper.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final SkuRepository skuRepository;

    @Transactional(readOnly = true)
    public List<CategoryDto> getTree() {
        List<Category> all = categoryRepository.findAllByDeletedAtIsNull();

        Map<Long, List<CategoryDto>> byParent = all.stream()
                .map(c -> new CategoryDto(
                        c.getId(),
                        c.getName(),
                        c.getSlug(),
                        c.getParentId(),
                        new ArrayList<>()))
                .collect(Collectors.groupingBy(dto -> dto.parentId() != null ? dto.parentId() : 0L));
        List<CategoryDto> roots = byParent.getOrDefault(0L, List.of());
        roots.forEach(root -> attachChildren(root, byParent));
        return roots;
    }

    @Transactional
    public CategoryDto create(CategoryRequest req) {
        if (categoryRepository.existsBySlugAndDeletedAtIsNull(req.slug())) {
            throw new ResourceAlreadyExistedException("Slug already existed");
        }

        if (req.parentId() != null) {
            categoryRepository.findByIdAndDeletedAtIsNull(req.parentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found"));
        }
        Category category = Category.builder()
                .name(req.name())
                .slug(req.slug())
                .parentId(req.parentId())
                .build();
        categoryRepository.save(category);
        return new CategoryDto(category.getId(), category.getName(), category.getSlug(), category.getParentId(), List.of());
    }

    @Transactional
    public CategoryDto update(Long id, CategoryRequest req) {
        Category category = categoryRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        if (!category.getSlug().equals(req.slug()) &&
                categoryRepository.existsBySlugAndDeletedAtIsNull(req.slug())) {
            throw new ResourceAlreadyExistedException("Slug already existed");
        }

        category.setName(req.name());
        category.setSlug(req.slug());
        categoryRepository.save(category);
        return new CategoryDto(category.getId(), category.getName(), category.getSlug(), category.getParentId(), List.of());
    }

    public void delete(Long id) {
        Category category = categoryRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        if (categoryRepository.existsByParentIdAndDeletedAtIsNull(id)) {
            throw new InvalidRequestException("Cannot delete category with subcategories");
        }
        if (skuRepository.existsByCategoryIdAndDeletedAtIsNull(id)) {
            throw new InvalidRequestException("Cannot delete sku with existing SKUs");
        }
        category.setDeletedAt(Instant.now());
        categoryRepository.save(category);
    }

    private void attachChildren(CategoryDto node, Map<Long, List<CategoryDto>> byParent) {
        List<CategoryDto> children = byParent.getOrDefault(node.id(), List.of());
        children.forEach(child -> attachChildren(child, byParent));
        node.children().addAll(children);
    }
}
