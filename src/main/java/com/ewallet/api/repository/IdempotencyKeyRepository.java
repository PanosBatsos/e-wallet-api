package com.ewallet.api.repository;

import com.ewallet.api.entity.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey , Long> {

    Optional<IdempotencyKey> findByUserIdAndIdempotencyKey(Long userId, String idempotencyKey);
}
