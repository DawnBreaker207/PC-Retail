package com.dawn.catalog.service;

import com.dawn.catalog.dto.CategoryDto;
import com.dawn.catalog.entity.Category;
import com.dawn.catalog.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryQueryService {

    private final CategoryRepository categoryRepository;

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

    private void attachChildren(CategoryDto node, Map<Long, List<CategoryDto>> byParent) {
        List<CategoryDto> children = byParent.getOrDefault(node.id(), List.of());
        children.forEach(child -> attachChildren(child, byParent));
        node.children().addAll(children);
    }
}
