package com.dawn.rma.port.in;

import com.dawn.rma.entity.ReturnRequest;

import java.util.List;

public interface QueryReturnUseCase {

    ReturnRequest get(Long id);

    ReturnRequest getBySubOrder(Long subOrderId, Long userId);

    List<ReturnRequest> staffList(String status);
}
