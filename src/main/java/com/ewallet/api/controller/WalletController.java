package com.ewallet.api.controller;

import com.ewallet.api.dto.transaction.TransactionResponseDTO;
import com.ewallet.api.dto.wallet.*;
import com.ewallet.api.entity.Wallet;
import com.ewallet.api.entity.WalletStatus;
import com.ewallet.api.service.TransactionService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import com.ewallet.api.service.WalletService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.security.Principal;

@RestController
@RequestMapping("/api/v0/wallets")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;
    private final TransactionService transactionService;


    /**
     * Processes a manual deposit into a user's wallet
     *  @param dto The deposit request dto
     * @return A ResponseEntity containing the details and 201 status
     */
    @PostMapping("/deposit")
    @Operation(summary = "Deposit into a wallet" , description = "Deposits an amount to a user's wallet")
    public ResponseEntity<WalletDepositResponseDTO> deposit(@Valid @RequestBody WalletDepositRequestDTO dto,
                                                            Principal principal){
        String userEmail = principal.getName();
        WalletDepositResponseDTO response = walletService.deposit(dto , userEmail);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/transfer")
    public ResponseEntity<WalletTransferResponseDTO> transfer(@Valid @RequestBody WalletTransferRequestDTO dto,
                                                              Principal principal) {
        String userEmail = principal.getName();
        WalletTransferResponseDTO response = walletService.transfer(dto , userEmail);
        return new ResponseEntity<>(response , HttpStatus.OK);
    }

    @PostMapping("/withdrawal")
    public ResponseEntity<WalletWithdrawalResponseDTO> withdrawal(@Valid @RequestBody WalletWithdrawalRequestDTO dto,
                                                                  Principal principal) {
        String userEmail = principal.getName();
        WalletWithdrawalResponseDTO response = walletService.withdrawal(dto , userEmail);
        return new ResponseEntity<>(response , HttpStatus.OK);
    }

    // Retrieves the paginated transaction history for the authenticated user
    // The history includes all deposits, withdrawals and transfers where the user is involved
    @GetMapping("/history")
    public ResponseEntity<Page<TransactionResponseDTO>> getHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal
    ) {
        String userEmail = principal.getName();
        Page<TransactionResponseDTO> response = transactionService.getTransactionHistory(userEmail,
                page,
                size);
        return new ResponseEntity<>(response , HttpStatus.OK);
    }
}
