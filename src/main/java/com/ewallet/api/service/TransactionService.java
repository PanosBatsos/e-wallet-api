package com.ewallet.api.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;


import com.ewallet.api.dto.kafka.TransactionEvent;
import com.ewallet.api.dto.transaction.TransactionMapper;
import com.ewallet.api.dto.transaction.TransactionResponseDTO;
import com.ewallet.api.entity.*;
import com.ewallet.api.repository.OutboxEventRepository;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.ewallet.api.repository.TransactionRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import static org.apache.kafka.common.requests.DeleteAclsResponse.log;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final ObjectMapper objectMapper;
    private final OutboxEventRepository outboxEventRepository;
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

        try {
            // From Event to JSON string
            String jsonPayload = objectMapper.writeValueAsString(event);

            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .aggregateType("TRANSACTION")
                    .aggregateId(String.valueOf(transaction.getId()))
                    .payload(jsonPayload)
                    .createdAt(LocalDateTime.now())
                    .processed(false)
                    .build();

            // Save to database
            outboxEventRepository.save(outboxEvent);
            log.info("Saved event to Outbox for Transaction ID: {}" , transaction.getId());
        } catch (Exception e) {
            log.error("Failed to serialize Outbox Event for transaction {}" , transaction.getId() , e);
            throw new RuntimeException("Failed to process transaction event" , e);
        }
    }
}

