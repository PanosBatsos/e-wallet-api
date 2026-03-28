package com.ewallet.api.dto.wallet;

import java.math.BigDecimal;
import java.time.LocalDateTime;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WalletDepositResponseDTO {
    private BigDecimal amountDeposited;
    private BigDecimal newBalance;
    private String currency;
    private LocalDateTime timestamp;
    private String message;
}
