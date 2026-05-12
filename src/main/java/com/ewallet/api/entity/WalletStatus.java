package com.ewallet.api.entity;

public enum WalletStatus {
    ACTIVE, // Wallet is operating normally
    FROZEN, // Temporary suspended
    LOCKED // Permanently locked or requires administrator intervention
}
