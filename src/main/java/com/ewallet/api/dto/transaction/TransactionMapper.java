package com.ewallet.api.dto.transaction;

import com.ewallet.api.entity.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransactionResponseDTO toResponseDTO(Transaction transaction) {
        String senderEmail = null;
        if (transaction.getSourceWallet() != null && transaction.getSourceWallet().getUser() != null) {
            senderEmail = transaction.getSourceWallet().getUser().getEmail();
        }

        String receiverEmail = null;
        if (transaction.getDestinationWallet() != null && transaction.getDestinationWallet().getUser() != null) {
            receiverEmail = transaction.getDestinationWallet().getUser().getEmail();
        }

        return TransactionResponseDTO.builder()
                .transactionId(transaction.getId())
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .status(transaction.getStatus())
                .timestamp(transaction.getTimestamp())
                .description(transaction.getDescription())
                .senderEmail(senderEmail)
                .receiverEmail(receiverEmail)
                .build();
    }
}
