package com.ewallet.api.exception;

import com.ewallet.api.dto.error.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // Handles cases where a requested resource is not found
    // Returns a 404 Not Found status
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex,
                                                   HttpServletRequest request) {
        return responseBuilder(ex.getMessage() , HttpStatus.NOT_FOUND , request);
    }

    // Handles cases where a transaction currency does not match the wallet currency.
    // Returns a 400 Bad Request status.
    @ExceptionHandler(CurrencyMismatchException.class)
    public ResponseEntity<ApiError> handleCurrencyMismatch(CurrencyMismatchException ex,
                                                           HttpServletRequest request) {
        return responseBuilder(ex.getMessage() , HttpStatus.BAD_REQUEST , request);
    }

    // Handles cases where a user already exists during registration
    // Returns a 409 Conflict status
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleUserConflict(UserAlreadyExistsException ex,
                                                       HttpServletRequest request) {
        return responseBuilder(ex.getMessage() , HttpStatus.CONFLICT , request);
    }


    // Handles errors triggered by @Valid
    // Collects all field errors and returns a 400 Bad Request status
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationErrors(MethodArgumentNotValidException ex,
                                                           HttpServletRequest request) {
        String errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " +error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return  responseBuilder("Validation failed: " + errors,
                HttpStatus.BAD_REQUEST,
                request);
    }

    // Helper method to build a response entity containing an ApiError.
    private ResponseEntity<ApiError> responseBuilder(String message , HttpStatus status ,
                                                     HttpServletRequest request) {
        ApiError apiError = ApiError.builder()
                .message(message)
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(apiError , status);
    }
}
