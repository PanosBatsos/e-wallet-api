package com.ewallet.api.exception;


public class TokenRefreshException extends RuntimeException{
    public TokenRefreshException(String message) {
        super(message);
    }
}
