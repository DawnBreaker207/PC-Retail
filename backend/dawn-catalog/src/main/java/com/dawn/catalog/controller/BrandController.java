package com.dawn.catalog.controller;

import com.dawn.catalog.controller.request.BrandRequest;
import com.dawn.catalog.dto.BrandDto;
import com.dawn.catalog.service.BrandCommandService;
import com.dawn.catalog.service.BrandQueryService;
import com.dawn.common.response.ResponseObject;
import com.dawn.common.security.AuthorizationConstants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/brands")
@RequiredArgsConstructor
public class BrandController {
    private final BrandCommandService brandCommandService;
    private final BrandQueryService brandQueryService;

    @GetMapping("")
    public ResponseObject<List<BrandDto>> getAll() {
        return ResponseObject.success(brandQueryService.getAll());
    }

    @PostMapping("")
    @PreAuthorize(AuthorizationConstants.ADMIN_ONLY)
    public ResponseObject<BrandDto> create(@Valid @RequestBody BrandRequest request) {
        return ResponseObject.created(brandCommandService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize(AuthorizationConstants.ADMIN_ONLY)
    public ResponseObject<BrandDto> update(@PathVariable Long id,
                                           @Valid @RequestBody BrandRequest request) {
        return ResponseObject.success(brandCommandService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(AuthorizationConstants.ADMIN_ONLY)
    public ResponseObject<Void> delete(@PathVariable Long id) {
        brandCommandService.delete(id);
        return ResponseObject.deleted();
    }
}
