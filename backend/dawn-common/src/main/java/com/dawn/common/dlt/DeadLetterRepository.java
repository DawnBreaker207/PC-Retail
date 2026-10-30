package com.dawn.common.dlt;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeadLetterRepository extends JpaRepository<DeadLetterRecord, Long> {
    List<DeadLetterRecord> findByStatusOrderByCreatedAtDesc(String status);
}
