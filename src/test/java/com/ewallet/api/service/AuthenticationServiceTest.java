package com.ewallet.api.service;

import com.ewallet.api.dto.user.UserRegisterRequestDTO;
import com.ewallet.api.exception.UserAlreadyExistsException;
import com.ewallet.api.repository.UserRepository;
import com.ewallet.api.repository.WalletRepository;
import com.ewallet.api.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private AuthenticationService authenticationService;



    @Test
    void register_ShouldThrowException_WhenEmailExists() {
        UserRegisterRequestDTO dto = new UserRegisterRequestDTO();
        dto.setEmail("test@test.com");
        dto.setFirstName("Panos");
        dto.setLastName("Batsos");

        when(userRepository.existsByEmail("test@test.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class , () -> {
            authenticationService.register(dto);
        });

        verify(userRepository , never()).save(any());
    }
}
