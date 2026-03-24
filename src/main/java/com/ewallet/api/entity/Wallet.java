package com.ewallet.api.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
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
    @OneToOne(optional = false)
    @JoinColumn(name = "user_id" , referencedColumnName = "id" , unique = true)
    private User user;
}
