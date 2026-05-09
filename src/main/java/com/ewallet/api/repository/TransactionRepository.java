package com.ewallet.api.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ewallet.api.entity.Transaction;


@Repository
public interface TransactionRepository extends JpaRepository<Transaction , Long> {
    /**
     * Retrieves a paginated list of transactions where the user is either the sender or the receiver
     * Jpa derives the query from the method name by traversing the associations:
     * Transaction -> SourceWallet/DestinationWallet -> User -> Email
     *
     * @param sourceEmail The email of the sender to find outgoing transactions
     * @param destEmail   The email of the receiver to find incoming transactions
     * @param pageable    Pagination and sorting information
     * @return A Page of Transactions
     */
    Page<Transaction> findAllBySourceWalletUserEmailOrDestinationWalletUserEmail(
            String sourceEmail,
            String destEmail,
            Pageable pageable
    ) ;
}
