package com.dawn.rma.infra.persistence;

import com.dawn.rma.entity.ReturnRequest;
import com.dawn.rma.port.out.ReturnRequestRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaReturnRequestRepository extends JpaRepository<ReturnRequest, Long>, ReturnRequestRepository {
    List<ReturnRequest> findBySubOrderId(Long subOrderId);

    List<ReturnRequest> findByStatus(com.dawn.rma.entity.ReturnStatus status);
}
