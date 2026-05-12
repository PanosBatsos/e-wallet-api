package com.ewallet.api.service;

import com.ewallet.api.dto.wallet.WalletDepositRequestDTO;
import com.ewallet.api.dto.wallet.WalletDepositResponseDTO;
import com.ewallet.api.dto.wallet.WalletTransferRequestDTO;
import com.ewallet.api.dto.wallet.WalletTransferResponseDTO;
import com.ewallet.api.entity.User;
import com.ewallet.api.entity.Wallet;
import com.ewallet.api.entity.WalletStatus;
import com.ewallet.api.repository.UserRepository;
import com.ewallet.api.repository.WalletRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WalletServiceTest {
    @Mock
    private WalletRepository walletRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private WalletService walletService;

    @Test
    void deposit_ShouldThrowException_WhenWalletIsFrozen() {
        String email = "user@test.com";

        // Create mock user
        User user = User.builder()
                .email(email)
                .build();

        // Create mock wallet
        Wallet wallet = Wallet.builder()
                .walletStatus(WalletStatus.FROZEN)
                .currency("EUR")
                .user(user)
                .build();

        // Create deposit request
        WalletDepositRequestDTO dto = WalletDepositRequestDTO.builder()
                .amount(new BigDecimal("100.00"))
                .currency("EUR")
                .description("")
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(walletRepository.findByUserEmail(email)).thenReturn(Optional.of(wallet));

        assertThrows(RuntimeException.class , () -> {
            walletService.deposit(dto , email);
        });
    }

    @Test
    void deposit_ShouldSucceed_WalletActive() {
        String email = "user@test.com";

        // Create mock user
        User user = User.builder()
                .email(email)
                .build();

        // Create mock wallet
        Wallet wallet = Wallet.builder()
                .walletStatus(WalletStatus.ACTIVE)
                .currency("EUR")
                .balance(BigDecimal.ZERO)
                .user(user)
                .build();

        // Create deposit request
        WalletDepositRequestDTO dto = WalletDepositRequestDTO.builder()
                .amount(new BigDecimal("100.00"))
                .currency("EUR")
                .description("")
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(walletRepository.findByUserEmail(email)).thenReturn(Optional.of(wallet));

        WalletDepositResponseDTO response = walletService.deposit(dto , email);

        // Verify the new balance is 100.00 and response message is Deposit Completed
        assertEquals(new BigDecimal("100.00") , wallet.getBalance());
        assertEquals("Deposit completed" , response.getMessage());

        // Verify that transactionService.recordDeposit was called once
        verify(transactionService , times(1)).recordDeposit(
                eq(wallet),
                eq(dto.getAmount()), // Request amount
                anyString(),
                eq(email)
        );
    }

    @Test
    void transfer_ShouldTransferMoneyWithoutProblem() {
        String senderEmail = "sender@test.com";
        String receiverEmail = "receiver@test.com";

        // Implement mock sender
        User sender = User.builder()
                .email(senderEmail)
                .build();

        // Implement mock receiver
        User receiver = User.builder()
                .email(receiverEmail)
                .build();

        // Implement sender's mock wallet with 1000 EUR
        Wallet senderWallet = Wallet.builder()
                .walletStatus(WalletStatus.ACTIVE)
                .balance(new BigDecimal("1000.00"))
                .currency("EUR")
                .user(sender)
                .build();

        // Implement receiver's mock wallet
        Wallet receiverWallet = Wallet.builder()
                .walletStatus(WalletStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
                .currency("EUR")
                .user(receiver)
                .build();

        WalletTransferRequestDTO transferRequest = new WalletTransferRequestDTO();
        transferRequest.setReceiverEmail(receiverEmail);
        transferRequest.setAmount(new BigDecimal("100.00"));
        transferRequest.setCurrency("EUR");
        transferRequest.setDescription("Transfer");


        when(walletRepository.findByUserEmail(senderEmail)).thenReturn(Optional.of(senderWallet));

        when(walletRepository.findByUserEmail(receiverEmail)).thenReturn(Optional.of(receiverWallet));


        WalletTransferResponseDTO response = walletService.transfer(transferRequest , senderEmail);

        // Check if the balances were updated correctly after the transfer
        assertEquals(new BigDecimal("900.00") , senderWallet.getBalance());
        assertEquals(new BigDecimal("100.00") , receiverWallet.getBalance());

        // Check if the response contains correct info
        // Emails and amount
        assertEquals(senderEmail , response.getSenderEmail());
        assertEquals(receiverEmail , response.getReceiverEmail());
        assertEquals(transferRequest.getAmount() , response.getAmountTransferred());

        // Verify that the transaction was recorded once
        verify(transactionService, times(1)).recordTransfer(
                eq(senderWallet),
                eq(receiverWallet),
                eq(transferRequest.getAmount()),
                eq(transferRequest.getDescription()),
                eq(senderEmail),
                eq(receiverEmail)
        );
    }


}


