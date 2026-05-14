package com.ewallet.api.service;

import com.ewallet.api.entity.RefreshToken;
import com.ewallet.api.entity.User;
import com.ewallet.api.exception.ResourceNotFoundException;
import com.ewallet.api.exception.TokenRefreshException;
import com.ewallet.api.repository.RefreshTokenRepository;
import com.ewallet.api.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Value("${jwt.refresh-expiration}")
    private Long refreshTokenDuration;

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    // Creates a new refresh token
    @Transactional
    public RefreshToken createRefreshToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("This user does not exist"));

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenDuration))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    // Verifies that the token used by user is not expired
    public RefreshToken verifyExpiration(RefreshToken refreshToken) {
        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new TokenRefreshException("Refresh Token expired.");
        }
        return refreshToken;
    }

    // Deletes the previous token
    @Transactional
    public void deleteByUserId(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("This user does not exist"));
        refreshTokenRepository.deleteByUser(user);
    }
}
