package com.ewallet.api.repository;

import com.ewallet.api.entity.User;
import com.ewallet.api.entity.UserRole;
import com.ewallet.api.entity.Wallet;
import com.ewallet.api.entity.WalletStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
public class WalletOptimisticLockingTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private WalletRepository walletRepository;

    @Test
    void shouldThrowException_WhenConcurrentUpdatesOccur() {
        String userEmail = "test@test.com";
        User user = User.builder()
                .birthDate(LocalDate.of(2005 , 2 , 28))
                .email(userEmail)
                .firstName("Test")
                .lastName("Test")
                .idCardNumber("AN123456")
                .taxNumber("123456789")
                .userRole(UserRole.USER)
                .password("123456")
                .build();

        testEntityManager.persist(user);

        Wallet wallet = Wallet.builder()
                .walletStatus(WalletStatus.ACTIVE)
                .balance(new BigDecimal("100.00"))
                .currency("EUR")
                .user(user)
                .build();

        // Persist and flush to assign an ID and a version (0)
        wallet = testEntityManager.persistAndFlush(wallet);
        Long walletId = wallet.getId();

        // Clear persistence context to ensure fetching fresh instances from the DB
        testEntityManager.clear();
        // Reads wallet with version 0
        Wallet wallet1 = walletRepository.findById(walletId).get();

        testEntityManager.clear();
        // Reads the same wallet with version 0
        Wallet wallet2 = walletRepository.findById(walletId).get();

        testEntityManager.clear();
        wallet1.setBalance(new BigDecimal("50.00"));
        walletRepository.saveAndFlush(wallet1);

        wallet2.setBalance(new BigDecimal("120.00"));

        // Hibernate should detect that the version in DB (1) is different from
        // the version in wallet2 object (0) and throw the exception
        assertThrows(ObjectOptimisticLockingFailureException.class, () -> {
            walletRepository.saveAndFlush(wallet2);
        });
    }
}
