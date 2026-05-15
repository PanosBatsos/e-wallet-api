package com.ewallet.api.service;

import com.ewallet.api.repository.IdempotencyKeyRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class IdempotencyKeyCleanupService {
    private final IdempotencyKeyRepository idempotencyKeyRepository;

    private static final int BATCH = 1000;
    private static final int CIRCLES = 100;

    @Scheduled(cron = "0 0  4 * * *")
    @Transactional
    public void cleanupExpiredKeys() {
        log.info("Starting batch cleanup for expired Idempotency keys");

        LocalDateTime expiryDate = LocalDateTime.now().minusDays(7);
        int totalDeleted = 0;
        int deletedInThisBatch;
        int circle = 0;

        do {
            deletedInThisBatch = idempotencyKeyRepository.deleteOldKeys(expiryDate , BATCH);
            totalDeleted += deletedInThisBatch;
            circle++;


        } while (deletedInThisBatch == BATCH && circle < CIRCLES);

        if (totalDeleted > 0) {
            log.info("Cleanup completed successfully. Removed {} expired keys in {} batches.", totalDeleted , circle);
        } else {
            log.info("Cleanup finished. No expired idempotency keys found.");
        }
    }
}
