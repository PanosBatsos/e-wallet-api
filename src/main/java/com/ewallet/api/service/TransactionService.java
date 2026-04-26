package com.ewallet.api.service;

import java.math.BigDecimal;


import com.ewallet.api.service.kafka.TransactionProducer;
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

    // Records a transaction and notifies the Kafka broker.
    // Currently supports only deposit. The other types will be added in the future.
    @Transactional
    public Transaction recordTransaction(Wallet wallet , TransactionType transactionType , BigDecimal amount , String description) {
        Transaction transaction = new Transaction();

        if (transactionType.equals(TransactionType.DEPOSIT)) {
            transaction.setAmount(amount);
            transaction.setDestinationWallet(wallet);
            transaction.setDescription(description);
            transaction.setSourceWallet(null);
            transaction.setType(transactionType);
            transaction.setStatus(TransactionStatus.SUCCESS); // (Sync for now) async logic to be added in the future
            Transaction savedTransaction = transactionRepository.save(transaction);

            // In the future a JSON object will be sent to Kafka
            // Right now a String is going to be sent only to verify the connection
            // and the end-to-end integration with the Dockerized Kafka broker
            String eventMessage = String.format(
                    "Event %s | TransactionID: %d | WalletID: %d | Amount: %s |Currency: %s",
                    transactionType,
                    savedTransaction.getId(),
                    wallet.getId(),
                    amount,
                    wallet.getCurrency()
            );

            transactionProducer.sendTransactionEvent(eventMessage);

            return savedTransaction;
        }
        // Other transaction cases will be added here in the future
        throw new UnsupportedOperationException("Transaction type not supported yet");
    }

}

