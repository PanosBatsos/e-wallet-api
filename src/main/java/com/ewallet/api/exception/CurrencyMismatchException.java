package com.ewallet.api.exception;

public class CurrencyMismatchException extends RuntimeException{
    public CurrencyMismatchException(String message) {
        super(message);
    }
}
