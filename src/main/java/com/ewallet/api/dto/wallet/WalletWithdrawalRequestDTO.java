package com.ewallet.api.dto.wallet;


import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WalletWithdrawalRequestDTO {

    // The amount of money to be transferred
    @NotNull(message = "Amount is required")
    @Positive(message = "The amount must be positive")
    @DecimalMin(value = "0.01" , message = "The minimum amount is 0.01")
    private BigDecimal amount;

    @NotNull(message = "ISO currency is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be 3 uppercase letters")
    private String currency;

    private String description;
}
