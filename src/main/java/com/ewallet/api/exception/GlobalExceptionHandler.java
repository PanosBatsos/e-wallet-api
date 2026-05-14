package com.ewallet.api.exception;

import com.ewallet.api.dto.error.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.ErrorResponse;
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

    // Generic exception handler for all other uncaught exceptions
    // During development errors currently handled here might be assigned to
    // their own specific handlers as the application grows.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleOtherExceptions(Exception ex,
                                                          HttpServletRequest request) {
        return responseBuilder("An unexpected error occurred" , HttpStatus.INTERNAL_SERVER_ERROR , request);
    }


    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiError> handleOptimisticLockingFailure(ObjectOptimisticLockingFailureException ex,
                                                                        HttpServletRequest request) {
        return responseBuilder("Transaction failed because the wallet balance was changed by another action at the same time",
                HttpStatus.CONFLICT,
                request);
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ApiError> handleInsufficientFunds(InsufficientFundsException ex,
                                                            HttpServletRequest request) {
        return responseBuilder(ex.getMessage() , HttpStatus.BAD_REQUEST , request);
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
