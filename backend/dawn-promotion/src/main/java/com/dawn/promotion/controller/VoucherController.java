package com.dawn.promotion.controller;

import com.dawn.common.response.ResponseObject;
import com.dawn.common.response.ResponsePage;
import com.dawn.common.security.AuthorizationConstants;
import com.dawn.promotion.controller.request.VoucherCreateRequest;
import com.dawn.promotion.controller.request.VoucherValidateRequest;
import com.dawn.promotion.dto.VoucherDto;
import com.dawn.promotion.dto.VoucherValidationResult;
import com.dawn.promotion.port.in.ManageVoucherUseCase;
import com.dawn.promotion.port.in.ValidatePromotionUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/vouchers")
@RequiredArgsConstructor
public class VoucherController {
    private final ManageVoucherUseCase voucherUseCase;
    private final ValidatePromotionUseCase validateUseCase;

    @GetMapping("")
    @PreAuthorize(AuthorizationConstants.STAFF_OR_ADMIN)
    public ResponseObject<ResponsePage<VoucherDto>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseObject.success(voucherUseCase.getAll(page, size));
    }

    @PostMapping("")
    @PreAuthorize(AuthorizationConstants.ADMIN_ONLY)
    public ResponseObject<VoucherDto> create(@Valid @RequestBody VoucherCreateRequest req) {
        return ResponseObject.created(voucherUseCase.create(req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(AuthorizationConstants.ADMIN_ONLY)
    public ResponseObject<Void> delete(@PathVariable Long id) {
        voucherUseCase.delete(id);
        return ResponseObject.deleted();
    }

    @PostMapping("/validate")
    @PreAuthorize(AuthorizationConstants.CUSTOMER_ONLY)
    public ResponseObject<VoucherValidationResult> validate(
            @Valid @RequestBody VoucherValidateRequest req) {
        return ResponseObject.success(validateUseCase.validate(req.code(), req.orderTotal()));
    }
}
