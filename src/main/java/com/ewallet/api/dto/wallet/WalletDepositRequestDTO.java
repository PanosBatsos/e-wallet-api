package com.ewallet.api.dto.wallet;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletDepositRequestDTO {
    
    // The amount of money to be deposited
    @NotNull(message = "Amount is required")
    @Positive(message = "The amount must be positive")
    @Min(value = 1 , message = "The minimum amount is 1")
    private BigDecimal amount;

    // The id of the user whose wallet will be cretided
    // Temp field until JWT security is implemented
    @NotNull(message = "User's id is required")
    private Long userId;

    @NotNull(message = "ISO currency is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be 3 uppercase letters")
    private String currency;

    private String description;
}
