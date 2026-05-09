package com.ewallet.api.dto.transaction;


import com.ewallet.api.entity.TransactionStatus;
import com.ewallet.api.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponseDTO {
    private Long transactionId;
    private BigDecimal amount;
    private TransactionType type; // Deposit, withdrawal, transfer
    private TransactionStatus status; // Success, pending, failed
    private LocalDateTime timestamp;
    private String description;
    private String senderEmail; // Null if type = deposit
    private String receiverEmail; // Null if type = withdrawal
}
