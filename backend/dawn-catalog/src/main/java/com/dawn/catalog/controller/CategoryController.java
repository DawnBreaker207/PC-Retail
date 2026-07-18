package com.dawn.catalog.controller;

import com.dawn.catalog.controller.request.CategoryRequest;
import com.dawn.catalog.dto.CategoryDto;
import com.dawn.catalog.service.CategoryService;
import com.dawn.common.response.ResponseObject;
import com.dawn.common.security.AuthorizationConstants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping("")
    public ResponseObject<List<CategoryDto>> getTree() {
        return ResponseObject.success(categoryService.getTree());
    }

    @PostMapping("")
    @PreAuthorize(AuthorizationConstants.ADMIN_ONLY)
    public ResponseObject<CategoryDto> create(@Valid @RequestBody CategoryRequest request) {
        return ResponseObject.created(categoryService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize(AuthorizationConstants.ADMIN_ONLY)
    public ResponseObject<CategoryDto> update(@PathVariable Long id,
                                              @Valid @RequestBody CategoryRequest request) {
        return ResponseObject.success(categoryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(AuthorizationConstants.ADMIN_ONLY)
    public ResponseObject<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseObject.deleted();
    }

}
