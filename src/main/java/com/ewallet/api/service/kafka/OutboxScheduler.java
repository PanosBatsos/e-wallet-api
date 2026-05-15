package com.ewallet.api.service.kafka;

import com.ewallet.api.dto.kafka.TransactionEvent;
import com.ewallet.api.entity.OutboxEvent;
import com.ewallet.api.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class OutboxScheduler {
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final TransactionProducer transactionProducer;

    /*
     * Polls the outbox table every 5 seconds to find pending events
     * and publish them to Kafka.
     */

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processOutboxEvents() {
        Pageable limit = PageRequest.of(0 , 100);
        List<OutboxEvent> pendingEvents = outboxEventRepository.findOutboxEventByProcessedFalseOrderByCreatedAtAsc(limit);

        if (pendingEvents.isEmpty()) {
            return; // Nothing to process
        }

        log.info("Found {} pending outbox events. Attempting to publish" , pendingEvents.size());

        for (OutboxEvent event : pendingEvents) {
            try {
                // Identify the event type
                if ("TRANSACTION".equals(event.getAggregateType())) {
                    // Convert JSON string to object
                    TransactionEvent transactionEvent = objectMapper.readValue(event.getPayload() , TransactionEvent.class);

                    // Hand over the event to the Producer to be sent to Kafka
                    transactionProducer.sendTransactionEvent(transactionEvent);
                }

                // Mark the event as processed to prevent re-sending
                event.setProcessed(true);

                log.info("Successfully published outbox event for aggregate ID: {}" , event.getAggregateId());
            } catch (Exception e) {
                // If publishing fails stop the loop and try again in 5 seconds
                log.error("Failed to process outbox event {}. Halting current execution.", event.getId(), e);
                break;
            }
        }
    }
}
