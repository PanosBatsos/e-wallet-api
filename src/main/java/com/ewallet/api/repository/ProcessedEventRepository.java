package com.ewallet.api.repository;

import com.ewallet.api.entity.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent , String> {
}
