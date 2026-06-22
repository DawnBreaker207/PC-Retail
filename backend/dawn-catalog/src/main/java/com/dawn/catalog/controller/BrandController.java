package com.dawn.catalog.controller;

import com.dawn.catalog.controller.request.BrandRequest;
import com.dawn.catalog.dto.BrandDto;
import com.dawn.catalog.service.BrandService;
import com.dawn.common.response.ResponseObject;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/brands")
@RequiredArgsConstructor
public class BrandController {
    private final BrandService brandService;

    @GetMapping("")
    public ResponseObject<List<BrandDto>> getAll() {
        return ResponseObject.success(brandService.getAll());
    }

    @PostMapping("")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseObject<BrandDto> create(@Valid @RequestBody BrandRequest request) {
        return ResponseObject.created(brandService.create(request));
    }

    @PutMapping("/{id}")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseObject<BrandDto> update(@PathVariable Long id,
                                           @Valid @RequestBody BrandRequest request) {
        return ResponseObject.success(brandService.update(id, request));
    }

    @DeleteMapping("/{id}")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseObject<Void> delete(@PathVariable Long id) {
        brandService.delete(id);
        return ResponseObject.deleted();
    }
}
