package com.ewallet.api.entity;

import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "wallets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Using BigDecimal for financial accuracy
    @Column(nullable = false)
    private BigDecimal balance;

    @Column(nullable = false , length = 3)
    private String currency; // ISO currency code e.g. USD , EUR 

    // Ono To One relation with the user (Every user has only one wallet)
    @OneToOne(optional = false , fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id" , referencedColumnName = "id" , unique = true)
    private User user;

    // Wallet status for security and transaction control
    @Enumerated(EnumType.STRING)
    private WalletStatus walletStatus = WalletStatus.ACTIVE;
}
