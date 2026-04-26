package com.ewallet.api.dto.kafka;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionEvent {
    private Long transactionId;
    private String type; // DEPOSIT...
    private Long sourceWalletId;
    private Long destinationWalletId;
    private BigDecimal amount;
    private String currency;
    private String description;
    private LocalDateTime timestamp;
}
