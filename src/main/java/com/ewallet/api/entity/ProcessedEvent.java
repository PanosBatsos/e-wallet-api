package com.ewallet.api.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;


// This entity acts as a de-duplication table
// It stores the Ids of kafka events that have been successfully processed
@Entity
@Table(name = "processed_events")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProcessedEvent {
    @Id
    private String eventId;

    private LocalDateTime processedAt;
}
