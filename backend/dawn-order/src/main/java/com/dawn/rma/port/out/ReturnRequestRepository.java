package com.dawn.rma.port.out;

import com.dawn.rma.entity.ReturnRequest;

import java.util.List;
import java.util.Optional;

public interface ReturnRequestRepository {

    Optional<ReturnRequest> findById(Long id);

    ReturnRequest save(ReturnRequest returnRequest);

    List<ReturnRequest> findBySubOrderId(Long subOrderId);

    List<ReturnRequest> findAll();
}
