package com.ewallet.api.repository;

import com.ewallet.api.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent , Long> {
    // Get all that have not been sent (processed = false) sorted from oldest to newest
    List<OutboxEvent> findOutboxEventByProcessedFalseOrderByCreatedAtAsc();
}
