package com.ewallet.api.service;



import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.ewallet.api.dto.wallet.*;
import com.ewallet.api.exception.CurrencyMismatchException;
import com.ewallet.api.exception.ResourceNotFoundException;
import com.ewallet.api.repository.WalletRepository;
import com.ewallet.api.service.kafka.TransactionProducer;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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
    private final WalletRepository walletRepository;

    /**
     * Processes a deposit request for a specific user
     * Validates user existence and ensures currency compability.
     * This method relies on Transactional annotation to automatically
     * persist the updated wallet balance to the database upon successful completion.
     * @param dto The deposit request payload
     * @return WalletDepositRequestDTO
     * @throws ResourceNotFoundException if the user is not found.
     * @throws CurrencyMismatchException  if there is a currency mismatch.
     */

    @Transactional
    public WalletDepositResponseDTO deposit(WalletDepositRequestDTO dto , String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User does not exist"));

        Wallet wallet = walletRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("This wallet does not exist"));

        // Ensures currency compatibility
        if (!dto.getCurrency().equals(wallet.getCurrency())) {
            throw new CurrencyMismatchException("Mismatch in currency");
        }

        // Update the balance
        wallet.setBalance(wallet.getBalance().add(dto.getAmount()));


        // Record the transaction for the audit trail.
        // If this operation fails the @Transactional will roll back the balance update.
        transactionService.recordDeposit(
                wallet,
                dto.getAmount(),
                dto.getDescription(),
                userEmail
        );


        return WalletDepositResponseDTO.builder()
            .amountDeposited(dto.getAmount())
            .currency(dto.getCurrency())
            .message("Deposit completed")
            .newBalance(wallet.getBalance())
            .timestamp(LocalDateTime.now())
            .build();
    }


    /**
     * Processes a fund transfer between two users' wallets
     * Validates user existence, sufficient funds and currency compatibility
     *
     * There is no transaction recording for now
     *
     * @param dto The transfer request payload
     * @param userEmail The email of the authenticated user initiating the transfer
     * @return WalletTransferResponseDTO containing the transfer details
     */


    @Transactional
    public WalletTransferResponseDTO transfer(WalletTransferRequestDTO dto , String userEmail) {

        Wallet senderWallet = walletRepository.findByUserEmail(userEmail).
                orElseThrow(() -> new ResourceNotFoundException("This wallet does not exist"));
        Wallet receiverWallet = walletRepository.findByUserEmail(dto.getReceiverEmail())
                .orElseThrow(() -> new ResourceNotFoundException("This user does not exist"));

        BigDecimal senderBalance = senderWallet.getBalance();
        // Check for insufficient balance
        if (senderBalance.compareTo(dto.getAmount()) < 0) {
            // InsufficientBalanceException will be added in the future
            throw new RuntimeException("Insufficient funds for this transfer");
        }



        // Check for currency mismatch
        if (!senderWallet.getCurrency().equals(receiverWallet.getCurrency())) {
            throw new CurrencyMismatchException("Cannot transfer between different currencies");
        }

        // Sync for now
        senderWallet.setBalance(senderWallet.getBalance().subtract(dto.getAmount()));
        receiverWallet.setBalance(receiverWallet.getBalance().add(dto.getAmount()));


        transactionService.recordTransfer(
                senderWallet,
                receiverWallet,
                dto.getAmount(),
                dto.getDescription(),
                userEmail,
                dto.getReceiverEmail()
        );

        return  WalletTransferResponseDTO.builder()
                .senderEmail(userEmail)
                .receiverEmail(dto.getReceiverEmail())
                .description(dto.getDescription())
                .amountTransferred(dto.getAmount())
                .timestamp(LocalDateTime.now())
                .currency(dto.getCurrency())
                .build();
                
    }

    @Transactional
    public WalletWithdrawalResponseDTO withdrawal(WalletWithdrawalRequestDTO dto , String userEmail) {
        Wallet wallet = walletRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("This wallet does not exist"));

        BigDecimal walletBalance =  wallet.getBalance();

        if (walletBalance.compareTo(dto.getAmount()) < 0) {
            // InsufficientBalanceException will be added in the future
            throw new RuntimeException("Insufficient funds for this transfer");
        }

        wallet.setBalance(wallet.getBalance().subtract(dto.getAmount()));

        transactionService.recordWithdrawal(
                wallet,
                dto.getAmount(),
                dto.getDescription(),
                userEmail
        );

        return  WalletWithdrawalResponseDTO.builder()
                .amountWithdrawn(dto.getAmount())
                .newBalance(wallet.getBalance())
                .message("Withdrawal completed successfully")
                .timestamp(LocalDateTime.now())
                .currency(wallet.getCurrency())
                .build();
    }
}
