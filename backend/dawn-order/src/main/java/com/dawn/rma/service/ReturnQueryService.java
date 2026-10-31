package com.dawn.rma.service;

import com.dawn.common.exception.wrapper.ResourceNotFoundException;
import com.dawn.rma.entity.ReturnRequest;
import com.dawn.rma.entity.ReturnStatus;
import com.dawn.rma.port.in.QueryReturnUseCase;
import com.dawn.rma.port.out.ReturnRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReturnQueryService implements QueryReturnUseCase {

    private final ReturnRequestRepository returnRepo;

    @Transactional(readOnly = true)
    public ReturnRequest get(Long id) {
        return returnRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ReturnRequest not found: " + id));
    }

    @Transactional(readOnly = true)
    public ReturnRequest getBySubOrder(Long subOrderId, Long userId) {
        return returnRepo.findBySubOrderId(subOrderId).stream()
                .filter(r -> r.getRequestedBy().equals(userId))
                .max(java.util.Comparator.comparing(ReturnRequest::getId))
                .orElseThrow(() -> new ResourceNotFoundException("No return request for sub-order " + subOrderId));
    }

    @Transactional(readOnly = true)
    public List<ReturnRequest> staffList(String status) {
        List<ReturnRequest> all = returnRepo.findAll();
        if (status == null || status.isBlank()) return all;
        final ReturnStatus s;
        try {
            s = ReturnStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            return all;
        }
        return all.stream().filter(r -> r.getStatus() == s).toList();
    }
}
