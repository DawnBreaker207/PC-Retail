package com.dawn.order.port.out;

import com.dawn.order.entity.ReturnRequest;
import com.dawn.order.entity.enums.ReturnStatus;

import java.util.List;
import java.util.Optional;

public interface ReturnRequestRepository {
    ReturnRequest save(ReturnRequest returnRequest);
    Optional<ReturnRequest> findById(Long id);
    Optional<ReturnRequest> findBySubOrderIdAndStatusIn(Long subOrderId, List<ReturnStatus> statuses);
    List<ReturnRequest> findByStatus(ReturnStatus status);
}
