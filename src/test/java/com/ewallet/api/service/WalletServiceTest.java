package com.ewallet.api.service;

import com.ewallet.api.dto.wallet.WalletDepositRequestDTO;
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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WalletServiceTest {
    @Mock
    private WalletRepository walletRepository;

    @Mock
    private UserRepository userRepository;

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
}


