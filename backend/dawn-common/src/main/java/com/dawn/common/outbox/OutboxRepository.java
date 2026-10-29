package com.dawn.common.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OutboxRepository extends JpaRepository<OutboxEvent, Long> {

    List<OutboxEvent> findTop100ByPublishedAtIsNullOrderByIdAsc();

    @Query(value = "SELECT * FROM outbox_events WHERE published_at IS NULL ORDER BY id ASC LIMIT :limit FOR UPDATE SKIP LOCKED", nativeQuery = true)
    List<OutboxEvent> findPendingEventsWithLock(@Param("limit") int limit);

    @org.springframework.data.jpa.repository.Modifying
    @Query("DELETE FROM OutboxEvent e WHERE e.publishedAt IS NOT NULL AND e.publishedAt < :threshold")
    int deleteOldPublishedEvents(@Param("threshold") java.time.Instant threshold);
}
