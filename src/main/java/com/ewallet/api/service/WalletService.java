package com.ewallet.api.service;



import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.ewallet.api.dto.wallet.WalletDepositRequestDTO;
import com.ewallet.api.dto.wallet.WalletDepositResponseDTO;
import com.ewallet.api.entity.TransactionType;
import com.ewallet.api.entity.User;
import com.ewallet.api.entity.Wallet;
import com.ewallet.api.repository.UserRepository;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WalletService {
    
    private final UserRepository userRepository;
    private final TransactionService transactionService;
    
    /**
     * Processes a deposit request for a specific user
     * Validates user existence and ensures currency compability.
     * This method relies on Transactional annotation to automatically 
     * persist the updated wallet balance to the database upon successful completion.
     * @param dto The deposit request payload
     * @return WalletDepositRequestDTO
     * @throws RuntimeException if the user is not found OR if there is a currency mismatch.
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


        
        transactionService.recordTransaction(wallet, 
            TransactionType.DEPOSIT,
             dto.getAmount(),
              dto.getDescription());



        return WalletDepositResponseDTO.builder()
            .amountDeposited(dto.getAmount())
            .currency(dto.getCurrency())
            .message("Deposit completed")
            .newBalance(wallet.getBalance())
            .timestamp(LocalDateTime.now())
            .build();
    }
}
