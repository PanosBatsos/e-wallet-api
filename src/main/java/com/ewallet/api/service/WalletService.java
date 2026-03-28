package com.ewallet.api.service;



import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.ewallet.api.dto.wallet.WalletDepositRequestDTO;
import com.ewallet.api.dto.wallet.WalletDepositResponseDTO;
import com.ewallet.api.entity.User;
import com.ewallet.api.entity.Wallet;
import com.ewallet.api.repository.UserRepository;
import com.ewallet.api.repository.WalletRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WalletService {
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;

    /**
     * Processes a deposit request for a specific user
     * Validates user existence and ensures currency compability
     * @param dto The deposit request
     * @throws RuntimeException if the user is not found if there is a currency mismatch 
     *  @return a deposit response dto
     * Exception handling to be added in the future and transaction logic
     */

    @Transactional
    public WalletDepositResponseDTO deposit(WalletDepositRequestDTO dto) {
        // Retrive the user from the database
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));   

        Wallet wallet = user.getWallet();
        
        // Ensures currency compability
        if (!dto.getCurrency().equals(wallet.getCurrency())) {
            throw new RuntimeException("Mismatch in currency");
        }

        // Update the balance
        wallet.setBalance(wallet.getBalance().add(dto.getAmount()));

        // Transaction will be added later...



        return WalletDepositResponseDTO.builder()
            .amountDeposited(dto.getAmount())
            .currency(dto.getCurrency())
            .message("Deposit completed")
            .newBalance(wallet.getBalance())
            .timestamp(LocalDateTime.now())
            .build();
    }
}
