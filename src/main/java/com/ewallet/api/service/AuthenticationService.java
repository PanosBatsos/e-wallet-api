package com.ewallet.api.service;

import com.ewallet.api.dto.user.AuthenticationResponse;
import com.ewallet.api.dto.user.RefreshTokenRequestDTO;
import com.ewallet.api.dto.user.UserLoginRequestDTO;
import com.ewallet.api.dto.user.UserRegisterRequestDTO;
import com.ewallet.api.entity.RefreshToken;
import com.ewallet.api.entity.User;
import com.ewallet.api.entity.Wallet;
import com.ewallet.api.exception.ResourceNotFoundException;
import com.ewallet.api.exception.UserAlreadyExistsException;
import com.ewallet.api.repository.UserRepository;
import com.ewallet.api.repository.WalletRepository;
import com.ewallet.api.security.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final WalletRepository walletRepository;
    private final RefreshTokenService refreshTokenService;
    /**
     * Registers a new user and automatically provisions an initial wallet for them
     * @param userRegisterRequestDTO The DTO containing registration details
     * @return AuthenticationResponse containing the token for the newly registered user
     */
    @Transactional
    public AuthenticationResponse register(UserRegisterRequestDTO userRegisterRequestDTO) {

        // Check if request's email is already used
        if (userRepository.existsByEmail(userRegisterRequestDTO.getEmail())) {
            throw new UserAlreadyExistsException("A user with this email already exists.");
        }

        // Check if request's Tax Num is already used
        if (userRepository.existsByTaxNumber(userRegisterRequestDTO.getTaxNumber())) {
            throw new UserAlreadyExistsException("A user with this Tax Number (AFM) already exists.");
        }

        // Check if request's id card number is already used
        if (userRepository.existsByIdCardNumber(userRegisterRequestDTO.getIdCardNumber())) {
            throw new UserAlreadyExistsException("A user with this ID Card Number already exists.");
        }
        // Build the User entity
        User user = User.builder()
                .firstName(userRegisterRequestDTO.getFirstName())
                .lastName(userRegisterRequestDTO.getLastName())
                .email(userRegisterRequestDTO.getEmail())
                .password(passwordEncoder.encode(userRegisterRequestDTO.getPassword()))
                .idCardNumber(userRegisterRequestDTO.getIdCardNumber())
                .taxNumber(userRegisterRequestDTO.getTaxNumber())
                .birthDate(userRegisterRequestDTO.getBirthDate())
                .build();

        // Save the user to implement the id
        User savedUser = userRepository.save(user);

        // Provision a new wallet with a default balance of zero
        Wallet wallet = Wallet.builder()
                .balance(BigDecimal.ZERO)
                .currency(userRegisterRequestDTO.getCurrency())
                .user(savedUser)
                .build();





        walletRepository.save(wallet);

        String jwtToken = jwtService.generateToken(user);

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getEmail());

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .refreshToken(refreshToken.getToken())
                .build();
    }


    /**
     * Authenticates a user based on email and password and returns a new JWT token
     * @param userLoginRequestDTO The dto containing login credentials
     * @return AuthenticationResponse containing the JWT token for the session
     */
    public AuthenticationResponse login(UserLoginRequestDTO userLoginRequestDTO) {

        // Authenticate the user credentials using spring security's AuthenticationManager
        // This process automatically hashes the provided password and compares it with the database
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        userLoginRequestDTO.getEmail(),
                        userLoginRequestDTO.getPassword()
                )
        );

        // Fetch the authenticated user from the database
        User user = userRepository.findByEmail(userLoginRequestDTO.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found after authentication"));

        // Delete the previous token
        refreshTokenService.deleteByUserId(user.getEmail());

        String jwtToken = jwtService.generateToken(user);

        // Create a new one
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getEmail());

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .refreshToken(refreshToken.getToken())
                .build();
    }


    // If the token is verified deletes the previous, creates a new one and
    // gives access token
    public AuthenticationResponse refreshToken(RefreshTokenRequestDTO requestDTO) {
        return refreshTokenService.findByToken(requestDTO.getRefreshToken())
                .map(refreshTokenService::verifyExpiration)
                .map(refreshToken -> {
                    User user = refreshToken.getUser();

                    refreshTokenService.deleteByUserId(user.getEmail());

                    String newAccessToken = jwtService.generateToken(user);
                    RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user.getEmail());

                    return AuthenticationResponse.builder()
                            .token(newAccessToken)
                            .refreshToken(newRefreshToken.getToken())
                            .build();
                })
                .orElseThrow(() -> new ResourceNotFoundException("Refresh Token not found"));
    }

    public boolean logout(String email) {
        try {
            refreshTokenService.deleteByUserId(email);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
