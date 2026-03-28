package com.ewallet.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ewallet.api.entity.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction , Long> {
    
}
