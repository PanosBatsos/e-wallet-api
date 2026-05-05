package com.ewallet.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ewallet.api.dto.user.UserRegisterRequestDTO;
import com.ewallet.api.dto.user.UserResponseDTO;
import com.ewallet.api.service.UserService;

import io.swagger.v3.oas.annotations.Operation;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v0/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Endpoints for user registration and profile management")
public class UserController {
    
    private final UserService userService;

    /**
     * Receives a validated dto triggers the business logic in the service
     * and returns the created user data
     * @param dto The validated user registration dto
     * @return A responseEntity containing the ResponseDTO and the HTTP status
     */
    /*
    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account and an associated wallet")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody UserRegisterRequestDTO dto) {
        UserResponseDTO response = userService.registerUser(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }*/
}
