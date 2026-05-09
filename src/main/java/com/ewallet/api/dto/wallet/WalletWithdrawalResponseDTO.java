package com.ewallet.api.dto.wallet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WalletWithdrawalResponseDTO {
    private BigDecimal amountWithdrawn;
    private BigDecimal newBalance;
    private String currency;
    private LocalDateTime timestamp;
    private String message;
}
