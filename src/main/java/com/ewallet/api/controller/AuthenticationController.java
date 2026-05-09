package com.ewallet.api.controller;

import com.ewallet.api.dto.user.AuthenticationResponse;
import com.ewallet.api.dto.user.RefreshTokenRequestDTO;
import com.ewallet.api.dto.user.UserLoginRequestDTO;
import com.ewallet.api.dto.user.UserRegisterRequestDTO;
import com.ewallet.api.service.AuthenticationService;
import com.ewallet.api.util.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
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
    private final CookieUtil cookieUtil;

    private static final int REFRESH_TOKEN_DURATION = 7 * 24 * 60 * 60; // 7 days

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@Valid @RequestBody UserRegisterRequestDTO dto,
                                                           HttpServletResponse response) {
        AuthenticationResponse authenticationResponse = authenticationService.register(dto);
        cookieUtil.addHttpOnlyCookie(response,
                "refresh_token",
                authenticationResponse.getRefreshToken(),
                REFRESH_TOKEN_DURATION);
        return new ResponseEntity<>(authenticationResponse , HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@Valid @RequestBody UserLoginRequestDTO dto,
                                                        HttpServletResponse response) {
        AuthenticationResponse authenticationResponse = authenticationService.login(dto);
        cookieUtil.addHttpOnlyCookie(response,
                "refresh_token",
                authenticationResponse.getRefreshToken(),
                REFRESH_TOKEN_DURATION);
        return new ResponseEntity<>(authenticationResponse , HttpStatus.OK);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refreshToken(@RequestBody RefreshTokenRequestDTO dto,
                                                               HttpServletResponse response) {
       AuthenticationResponse authenticationResponse = authenticationService.refreshToken(dto);
        cookieUtil.addHttpOnlyCookie(response,
                "refresh_token",
                authenticationResponse.getRefreshToken(),
                REFRESH_TOKEN_DURATION);
       return new ResponseEntity<>(authenticationResponse , HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        cookieUtil.deleteCookie(response, "refresh_token");
        return ResponseEntity.noContent().build();
    }
}
