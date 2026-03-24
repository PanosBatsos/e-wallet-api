package com.ewallet.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ewallet.api.entity.Wallet;

@Repository
public interface WalletRepository extends JpaRepository<Wallet , Long>{
    Optional<Wallet> findByUserId(Long userId); // Finds the wallet belonging to a specific user ID
    // Returns an Optional to handle cases where a wallet may not exist yet
}
