package com.ewallet.api.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;


import com.ewallet.api.dto.kafka.TransactionEvent;
import com.ewallet.api.dto.transaction.TransactionMapper;
import com.ewallet.api.dto.transaction.TransactionResponseDTO;
import com.ewallet.api.service.kafka.TransactionProducer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.ewallet.api.entity.Transaction;
import com.ewallet.api.entity.TransactionStatus;
import com.ewallet.api.entity.TransactionType;
import com.ewallet.api.entity.Wallet;
import com.ewallet.api.repository.TransactionRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final TransactionProducer transactionProducer;
    private final TransactionMapper transactionMapper;

    @Transactional
    public TransactionResponseDTO recordDeposit(Wallet wallet , BigDecimal amount , String description , String userEmail) {
        Transaction transaction = Transaction.builder()
                .amount(amount)
                .status(TransactionStatus.SUCCESS) // For now
                .sourceWallet(null)
                .destinationWallet(wallet)
                .description(description)
                .type(TransactionType.DEPOSIT)
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);

        sendAsyncEvent(savedTransaction);

        return TransactionResponseDTO.builder()
                .transactionId(savedTransaction.getId())
                .senderEmail(null)
                .receiverEmail(userEmail)
                .amount(savedTransaction.getAmount())
                .status(savedTransaction.getStatus())
                .type(savedTransaction.getType())
                .timestamp(savedTransaction.getTimestamp())
                .build();
    }

    @Transactional
    public TransactionResponseDTO recordTransfer(Wallet sourceWallet , Wallet destinationWallet , BigDecimal amount , String description , String senderEmail , String receiverEmail) {
        Transaction transaction = Transaction.builder()
                .amount(amount)
                .status(TransactionStatus.SUCCESS) // For now
                .sourceWallet(sourceWallet)
                .destinationWallet(destinationWallet)
                .description(description)
                .type(TransactionType.TRANSFER)
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);

        sendAsyncEvent(savedTransaction);

        return TransactionResponseDTO.builder()
                .transactionId(savedTransaction.getId())
                .senderEmail(senderEmail)
                .receiverEmail(receiverEmail)
                .amount(savedTransaction.getAmount())
                .status(savedTransaction.getStatus())
                .type(savedTransaction.getType())
                .timestamp(savedTransaction.getTimestamp())
                .build();
    }

    @Transactional
    public TransactionResponseDTO recordWithdrawal(Wallet wallet , BigDecimal amount , String description , String senderEmail) {
        Transaction transaction = Transaction.builder()
                .amount(amount)
                .status(TransactionStatus.SUCCESS) // For now
                .sourceWallet(wallet)
                .destinationWallet(null)
                .description(description)
                .type(TransactionType.WITHDRAWAL)
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);

        sendAsyncEvent(savedTransaction);

        return TransactionResponseDTO.builder()
                .transactionId(savedTransaction.getId())
                .senderEmail(senderEmail)
                .receiverEmail(null)
                .amount(savedTransaction.getAmount())
                .status(savedTransaction.getStatus())
                .type(savedTransaction.getType())
                .timestamp(savedTransaction.getTimestamp())
                .build();
    }


    // Retrieves a paginated list of transaction history for a specific user email
    // Maps the database entities to DTOs for safe data exposure
    public Page<TransactionResponseDTO> getTransactionHistory(String email , int page , int size) {
        Pageable pageable = PageRequest.of(page , size , Sort.by("timestamp").descending());

        Page<Transaction> transactionPage = transactionRepository
                .findAllBySourceWalletUserEmailOrDestinationWalletUserEmail(email , email , pageable);

        return transactionPage.map(transactionMapper::toResponseDTO);
    }


    private void sendAsyncEvent(Transaction transaction) {
        Long sourceId = null;
        if (transaction.getSourceWallet() != null) {
            sourceId = transaction.getSourceWallet().getId();
        }


        Long destinationId = null;
        if (transaction.getDestinationWallet() != null) {
            destinationId = transaction.getDestinationWallet().getId();
        }


        String eventCurrency = "";
        if (transaction.getDestinationWallet() != null) {
            eventCurrency = transaction.getDestinationWallet().getCurrency();
        } else if (transaction.getSourceWallet() != null){
            eventCurrency = transaction.getSourceWallet().getCurrency();
        }

        TransactionEvent event = TransactionEvent.builder()
                .transactionId(transaction.getId())
                .sourceWalletId(sourceId)
                .destinationWalletId(destinationId)
                .timestamp(transaction.getTimestamp())
                .type(transaction.getType().name())
                .currency(eventCurrency)
                .description(transaction.getDescription())
                .amount(transaction.getAmount())
                .build();

        transactionProducer.sendTransactionEvent(event);
    }
}

