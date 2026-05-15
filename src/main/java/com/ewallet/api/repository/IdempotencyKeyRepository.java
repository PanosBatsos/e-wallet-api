package com.ewallet.api.repository;

import com.ewallet.api.entity.IdempotencyKey;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey , Long> {

    Optional<IdempotencyKey> findByUserIdAndIdempotencyKey(Long userId, String idempotencyKey);

    @Modifying
    @Query(nativeQuery = true, value = """
        DELETE FROM idempotency_keys WHERE id IN (
            SELECT id FROM idempotency_keys 
            WHERE created_at < :expiryDate 
            LIMIT :limit
        )
    """)
    int deleteOldKeys(@Param("expiryDate")LocalDateTime expiryDate , @Param("limit") int limit);
}
