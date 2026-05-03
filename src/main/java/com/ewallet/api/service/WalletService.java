package com.ewallet.api.service;



import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.ewallet.api.dto.wallet.WalletTransferRequestDTO;
import com.ewallet.api.dto.wallet.WalletTransferResponseDTO;
import com.ewallet.api.exception.CurrencyMismatchException;
import com.ewallet.api.exception.ResourceNotFoundException;
import com.ewallet.api.service.kafka.TransactionProducer;
import org.springframework.http.ResponseEntity;
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
     * @throws ResourceNotFoundException if the user is not found.
     * @throws CurrencyMismatchException  if there is a currency mismatch.
     */

    @Transactional
    public WalletDepositResponseDTO deposit(WalletDepositRequestDTO dto , String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User does not exist"));

        Wallet wallet = user.getWallet();

        // Ensures currency compatibility
        if (!dto.getCurrency().equals(wallet.getCurrency())) {
            throw new CurrencyMismatchException("Mismatch in currency");
        }

        // Update the balance
        wallet.setBalance(wallet.getBalance().add(dto.getAmount()));


        // Record the transaction for the audit trail.
        // If this operation fails the @Transactional will roll back the balance update.
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


    /**
     * Processes a fund transfer between two users' wallets
     * Validates user existence, sufficient funds and currency compatibility
     *
     * The current transaction recording is synchronous and serves
     * as a baseline for testing security and business logic
     * In the future this will be refactored to publish TransactionEvents
     * to Kafka for asynchronous processing and event-driven architecture
     *
     * @param dto The transfer request payload
     * @param senderEmail The email of the authenticated user initiating the transfer
     * @return WalletTransferResponseDTO containing the transfer details
     */


    @Transactional
    public WalletTransferResponseDTO transfer(WalletTransferRequestDTO dto , String senderEmail) {

        // Check for sender's and receiver's existence
        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new ResourceNotFoundException("This user does not exist"));

        User receiver = userRepository.findByEmail(dto.getReceiverEmail())
                .orElseThrow(() -> new ResourceNotFoundException("This user does not exist"));

        BigDecimal senderBalance = sender.getWallet().getBalance();

        // Check for insufficient balance
        if (senderBalance.compareTo(dto.getAmount()) < 0) {
            // InsufficientBalanceException will be added in the future
            throw new RuntimeException("Insufficient funds for this transfer");
        }

        Wallet senderWallet = sender.getWallet();
        Wallet receiverWallet = receiver.getWallet();

        // Check for currency mismatch
        if (!senderWallet.getCurrency().equals(receiverWallet.getCurrency())) {
            throw new CurrencyMismatchException("Cannot transfer between different currencies");
        }

        // Sync for now
        senderWallet.setBalance(senderWallet.getBalance().subtract(dto.getAmount()));
        receiverWallet.setBalance(receiverWallet.getBalance().add(dto.getAmount()));

        transactionService.recordTransaction(senderWallet,
                TransactionType.TRANSFER, // Consider using TRANSFER_ΟUT in the future
                dto.getAmount(),
         dto.getDescription());

        transactionService.recordTransaction(receiverWallet,
                TransactionType.TRANSFER, // Consider using TRANSFER_IN in the future
                dto.getAmount(),
                dto.getDescription());

        return  WalletTransferResponseDTO.builder()
                .senderEmail(senderEmail)
                .receiverEmail(receiver.getEmail())
                .description(dto.getDescription())
                .amountTransferred(dto.getAmount())
                .timestamp(LocalDateTime.now())
                .currency(dto.getCurrency())
                .build();
                
    }
}
