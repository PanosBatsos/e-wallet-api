package com.ewallet.api.dto.wallet;

import com.ewallet.api.entity.WalletStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletStatusChangeResponseDTO {
    private String message;
    private WalletStatus newStatus;
    private Long walletId;
    private LocalDateTime timestamp;
}
