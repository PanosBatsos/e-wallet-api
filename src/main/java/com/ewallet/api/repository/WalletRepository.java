package com.ewallet.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ewallet.api.entity.Wallet;

@Repository
public interface WalletRepository extends JpaRepository<Wallet , Long>{
    // Finds the wallet by its user's email
    Optional<Wallet> findByUserEmail(String email);

}
