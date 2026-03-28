package com.ewallet.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import com.ewallet.api.dto.wallet.WalletDepositRequestDTO;
import com.ewallet.api.dto.wallet.WalletDepositResponseDTO;
import com.ewallet.api.service.WalletService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v0/wallets")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService; 


    /**
     * Processes a manual deposit into a user's wallet
     *  @param dto The deposit request dto
     * @return A ResponseEntity containing the details and 201 status
     */
    @PostMapping("/deposit")
    @Operation(summary = "Deposit into a wallet" , description = "Deposits an amount to a user's wallet")
    public ResponseEntity<WalletDepositResponseDTO> deposit(@Valid @RequestBody WalletDepositRequestDTO dto){
        WalletDepositResponseDTO response = walletService.deposit(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

}
