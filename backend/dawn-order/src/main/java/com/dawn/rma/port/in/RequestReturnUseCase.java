package com.dawn.rma.port.in;

import com.dawn.rma.entity.ReturnRequest;

import java.math.BigDecimal;
import java.util.List;

public interface RequestReturnUseCase {

    ReturnRequest request(Long subOrderId, Long userId, String reason, List<String> images);

    ReturnRequest dispute(Long id, Long userId, String note);

    ReturnRequest resolveDispute(Long id, Long adminId, boolean overturn, String note);

    ReturnRequest approve(Long id, Long staffId, BigDecimal amount, String method);

    ReturnRequest reject(Long id, Long staffId);
}
