package com.ewallet.api.repository;

import com.ewallet.api.entity.OutboxEvent;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.List;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent , Long> {
    // Get all that have not been sent (processed = false) sorted from oldest to newest
    List<OutboxEvent> findOutboxEventByProcessedFalseOrderByCreatedAtAsc(Pageable pageable);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM outbox_events WHERE id IN (" +
            "SELECT id FROM outbox_events WHERE processed = true " +
            "ORDER BY created_at ASC LIMIT :limit)" , nativeQuery = true)
    int deleteProcessedEventsBatch(@Param("limit") int limit);
}
