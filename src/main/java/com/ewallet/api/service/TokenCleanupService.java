package com.ewallet.api.entity;

import com.ewallet.api.repository.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupService {
    private final RefreshTokenRepository refreshTokenRepository;
    private static final int BATCH_SIZE = 1000;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanExpiredTokens() {
        log.info("Cleanup of expired refresh tokens begins");

        int totalDeleted = 0;
        int deletedInThisBatch;
        final int MAX_CIRCLES = 100;
        int currentCircle = 0;

        do {
            deletedInThisBatch = refreshTokenRepository.deleteExpiredTokensInBatches(Instant.now(), BATCH_SIZE);
            totalDeleted = totalDeleted + deletedInThisBatch;
            currentCircle++;
        } while (deletedInThisBatch == BATCH_SIZE && currentCircle < MAX_CIRCLES);

        if (totalDeleted > 0) {
            log.info("Deleted {} tokens" , totalDeleted );
        } else {
            log.info("There are not expired tokens ");
        }
    }
}
