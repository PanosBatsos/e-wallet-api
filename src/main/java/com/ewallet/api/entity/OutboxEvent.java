package com.ewallet.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "outbox_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // e.g. Transaction (What type of event is it)
    @Column(nullable = false)
    private String aggregateType;

    // Id of transaction
    @Column(nullable = false)
    private String aggregateId;

    // The DTO as JSON string
    @Column(columnDefinition = "TEXT" , nullable = false)
    private String payload;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private boolean processed;
}
