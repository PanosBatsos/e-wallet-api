package com.ewallet.api.dto.wallet;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class WalletTransferRequestDTO {
    // Tmp field until JWT security is implemented
    @NotNull(message = "User's id is required")
    private Long sourceUserId;

    // The id of the other user
    @NotNull(message = "Destination user's id is required")
    private Long destinationUserId;

    // The amount of money to be transferred
    @NotNull(message = "Amount is required")
    @Positive(message = "The amount must be positive")
    @Min(value = 1 , message = "The minimum amount is 1")
    private BigDecimal amount;

    @NotNull(message = "ISO currency is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be 3 uppercase letters")
    private String currency;

    private String description;
}
