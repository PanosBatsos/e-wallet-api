package com.ewallet.api.repository;

import com.ewallet.api.entity.RefreshToken;
import com.ewallet.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.time.Instant;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken (String token);

    @Modifying
    void deleteByUser(User user);

    @Modifying
    @Query(nativeQuery = true,
    value = """
DELETE FROM refresh_tokens
WHERE id IN (
    SELECT id FROM refresh_tokens
              WHERE expiry_date <= :now
              LIMIT :batchSize
              )
              """)
    int deleteExpiredTokensInBatches(@Param("now") Instant now , @Param("batchSize") int batchSize);
}
