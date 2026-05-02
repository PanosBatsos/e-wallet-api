package com.ewallet.api.dto.wallet;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class WalletTransferResponseDTO {
    private BigDecimal amountDeposited;
    private BigDecimal newBalance;
    private String currency;
    private LocalDateTime timestamp;
    private String message;
    private Long sourceUserId;
    private Long destinationUserId;
}
