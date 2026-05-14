package com.ewallet.api.exception;

public class WalletInactiveException extends RuntimeException{
    public WalletInactiveException(String message) {
        super(message);
    }
}
