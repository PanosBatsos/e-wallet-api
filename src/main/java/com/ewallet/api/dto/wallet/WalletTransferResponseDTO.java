package com.ewallet.api.dto.wallet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WalletTransferResponseDTO {
    private BigDecimal amountTransferred;
    private String currency;
    private LocalDateTime timestamp;
    private String description;
    private String senderEmail;
    private String receiverEmail;
}
