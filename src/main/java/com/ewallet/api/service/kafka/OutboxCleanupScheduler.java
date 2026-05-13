package com.ewallet.api.service.kafka;

import com.ewallet.api.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class OutboxCleanupScheduler {
    private final OutboxEventRepository outboxEventRepository;

    private static final int BATCH = 100;
    private static final int MAX_CIRCLES = 5000;

    /**
     * Scheduled task that runs every day at 5:00 ΑΜ
     * Cleans up outbox events that have already been published to kafka
     * using a batched approach to avoid database locking
     */

    @Scheduled(cron = "0 0 5 * * *")
    public void cleanup() {
        log.info("Starting outbox cleanup...");

        int totalDeleted = 0;
        int deletedInThisBatch;
        int currentCircle = 0;

        do {
            deletedInThisBatch = outboxEventRepository.deleteProcessedEventsBatch(BATCH);
            totalDeleted += deletedInThisBatch;
            currentCircle++;

        } while (deletedInThisBatch == BATCH && currentCircle < MAX_CIRCLES);

        if (totalDeleted > 0) {
            log.info("Cleanup completed. Total records removed: {}", totalDeleted);
        } else {
            log.info("Cleanup finished. No processed events found.");
        }
    }

}
