package com.ewallet.api.repository;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ewallet.api.entity.Wallet;

@Repository
public interface WalletRepository extends JpaRepository<Wallet , Long>{
    // Finds the wallet by its user's email
    Optional<Wallet> findByUserEmail(String email);

    /**
     * Executes an atomic database-level update to subtract money from a wallet
     * This query prevents race conditions and ensuring data integrity under
     * high concurrency.
     *
     * @param id The id of the wallet to be debited
     * @param amount The amount of money to subtract from wallet's balance
     * @return The number of rows updated, returns 1 on success or 0 if the wallet
     *  does not exist or if the balance is insufficient.
     */
    @Modifying
    @Query("UPDATE Wallet w Set w.balance = w.balance - :amount, w.version = w.version + 1 " +
            /**
             * Since the executed UPDATE query is custom Hibernate's automatic
             * dirty checking and versioning are bypassed. The version field must
             * be manually incremented to keep the Optimistic Locking mechanism consistent.
             */
    "WHERE w.id = :id AND w.balance >= :amount")
           /**
            * This condition guarantees at the database level that the balance will
            * never drop below zero. If multiple threads try to debit the same wallet
            * simultaneously, the database engine processes these atomically.
            */
    int debitWallet(@Param("id") Long id , @Param("amount") BigDecimal amount);

    @Modifying
    @Query("UPDATE Wallet w Set w.balance = w.balance + :amount, w.version = w.version + 1 " +
    "WHERE w.id = :id")
    int creditWallet(@Param("id") Long id , @Param("amount") BigDecimal amount);
}
