package com.ewallet.api.service;

import com.ewallet.api.dto.wallet.WalletDepositRequestDTO;
import com.ewallet.api.dto.wallet.WalletDepositResponseDTO;
import com.ewallet.api.entity.TransactionType;
import com.ewallet.api.entity.User;
import com.ewallet.api.entity.Wallet;
import com.ewallet.api.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WalletServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private WalletService walletService;


    /**
     * Tests the successful deposit scenario and verifies
     * that balance is updated correctly and transaction is recorded.
     */
    @Test
    void deposit_Success() {
        // Set up mock data
        Long userId = 1L;
        BigDecimal balance = new BigDecimal("100.00");
        BigDecimal depositAmount = new BigDecimal("30.00");
        String currency = "EUR";


        // Create mock Wallet and User
        Wallet wallet = new Wallet();
        wallet.setBalance(balance);
        wallet.setCurrency(currency);

        User user = new User();
        user.setId(userId);
        user.setWallet(wallet);

        // Create DTO of the request
        WalletDepositRequestDTO requestDto = new WalletDepositRequestDTO();
        requestDto.setUserId(userId);
        requestDto.setAmount(depositAmount);
        requestDto.setCurrency(currency);
        requestDto.setDescription("Monthly Salary");

        // Tell the mock repository to return the mock user
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Execute the service method
        WalletDepositResponseDTO responseDTO = walletService.deposit(requestDto);

        // Verifies results and behavior
        assertNotNull(responseDTO);
        assertEquals(new BigDecimal("130.00") , responseDTO.getNewBalance());
        assertEquals("Deposit completed" , responseDTO.getMessage());

        // Ensure the transaction was logged via the TransactionService
        verify(transactionService , times(1)).recordTransaction(
                eq(wallet),
                eq(TransactionType.DEPOSIT),
                eq(depositAmount),
                anyString()
        );
    }
}


