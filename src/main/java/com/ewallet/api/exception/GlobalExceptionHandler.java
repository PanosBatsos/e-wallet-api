package com.ewallet.api.exception;

import com.ewallet.api.dto.error.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // Handles cases where a requested resource is not found
    // Returns a 404 Not Found status
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex,
                                                   HttpServletRequest request) {
        return responseBuilder(ex.getMessage() , HttpStatus.NOT_FOUND , request);
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
