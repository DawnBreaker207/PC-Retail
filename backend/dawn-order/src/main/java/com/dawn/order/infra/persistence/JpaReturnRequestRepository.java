package com.dawn.order.infra.persistence;

import com.dawn.order.entity.ReturnRequest;
import com.dawn.order.entity.enums.ReturnStatus;
import com.dawn.order.port.out.ReturnRequestRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaReturnRequestRepository extends JpaRepository<ReturnRequest, Long>, ReturnRequestRepository {

    @Override
    Optional<ReturnRequest> findBySubOrderIdAndStatusIn(Long subOrderId, List<ReturnStatus> statuses);

    @Override
    List<ReturnRequest> findByStatus(ReturnStatus status);
}
