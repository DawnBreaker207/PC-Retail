package com.dawn.promotion.controller;

import com.dawn.common.response.ResponseObject;
import com.dawn.common.response.ResponsePage;
import com.dawn.promotion.controller.request.VoucherCreateRequest;
import com.dawn.promotion.controller.request.VoucherValidateRequest;
import com.dawn.promotion.dto.VoucherDto;
import com.dawn.promotion.dto.VoucherValidationResult;
import com.dawn.promotion.service.VoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("vouchers")
@RequiredArgsConstructor
public class VoucherController {
    private final VoucherService voucherService;

    @GetMapping("")
    public ResponseObject<ResponsePage<VoucherDto>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseObject.success(voucherService.getAll(page, size));
    }

    @PostMapping("")
    public ResponseObject<VoucherDto> create(@Valid @RequestBody VoucherCreateRequest req) {
        return ResponseObject.created(voucherService.create(req));
    }

    @DeleteMapping("/{id}")
    public ResponseObject<Void> delete(@PathVariable Long id) {
        voucherService.delete(id);
        return ResponseObject.deleted();
    }

    @PostMapping("/validate")
    public ResponseObject<VoucherValidationResult> validate(
            @Valid @RequestBody VoucherValidateRequest req) {
        return ResponseObject.success(voucherService.validate(req.code(), req.orderTotal()));
    }
}
