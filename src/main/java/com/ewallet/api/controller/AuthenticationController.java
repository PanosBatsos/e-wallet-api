package com.ewallet.api.controller;

import com.ewallet.api.dto.user.AuthenticationResponse;
import com.ewallet.api.dto.user.RefreshTokenRequestDTO;
import com.ewallet.api.dto.user.UserLoginRequestDTO;
import com.ewallet.api.dto.user.UserRegisterRequestDTO;
import com.ewallet.api.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v0/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@Valid @RequestBody UserRegisterRequestDTO dto) {
        AuthenticationResponse authenticationResponse = authenticationService.register(dto);
        return new ResponseEntity<>(authenticationResponse , HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@Valid @RequestBody UserLoginRequestDTO dto) {
        AuthenticationResponse authenticationResponse = authenticationService.login(dto);
        return new ResponseEntity<>(authenticationResponse , HttpStatus.OK);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refreshToken(@RequestBody RefreshTokenRequestDTO dto) {
       AuthenticationResponse authenticationResponse = authenticationService.refreshToken(dto);
       return new ResponseEntity<>(authenticationResponse , HttpStatus.OK);
    }
}
