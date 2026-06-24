package com.dawn.promotion.port.in;

import com.dawn.common.response.ResponsePage;
import com.dawn.promotion.controller.request.VoucherCreateRequest;
import com.dawn.promotion.dto.VoucherDto;

public interface ManageVoucherUseCase {
    VoucherDto create(VoucherCreateRequest req);

    ResponsePage<VoucherDto> getAll(int page, int size);

    void delete(Long id);
}
