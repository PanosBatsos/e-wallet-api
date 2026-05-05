package com.ewallet.api.service;

import com.ewallet.api.dto.user.AuthenticationResponse;
import com.ewallet.api.dto.user.UserLoginRequestDTO;
import com.ewallet.api.dto.user.UserRegisterRequestDTO;
import com.ewallet.api.entity.RefreshToken;
import com.ewallet.api.entity.User;
import com.ewallet.api.exception.UserAlreadyExistsException;
import com.ewallet.api.repository.UserRepository;
import com.ewallet.api.repository.WalletRepository;
import com.ewallet.api.security.JwtService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

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

    @Mock
    private RefreshTokenService refreshTokenService;

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


    @Test
    void register_ShouldSaveUser_WhenDataIsValid() {
        UserRegisterRequestDTO dto = new UserRegisterRequestDTO();
        dto.setEmail("newuser@test.com");
        dto.setFirstName("Panos");
        dto.setLastName("Batsos");
        dto.setPassword("Secret123!");
        dto.setIdCardNumber("AN123456");
        dto.setTaxNumber("123456789");

        when(userRepository.existsByEmail("newuser@test.com")).thenReturn(false);
        when(passwordEncoder.encode("Secret123!")).thenReturn("hashedPassword123");
        when(userRepository.existsByIdCardNumber("AN123456")).thenReturn(false);
        when(userRepository.existsByTaxNumber("123456789")).thenReturn(false);


        when(jwtService.generateToken(any())).thenReturn("fake-jwt-token");

        RefreshToken dummyRefreshToken = new RefreshToken();
        dummyRefreshToken.setToken("fake-refresh-token");

        when(refreshTokenService.createRefreshToken(anyString())).thenReturn(dummyRefreshToken);

        authenticationService.register(dto);

        verify(userRepository, times(1)).save(any());
    }

    @Test
    void register_ShouldThrowException_WhenTaxNumExists() {
        UserRegisterRequestDTO dto = new UserRegisterRequestDTO();
        dto.setEmail("newuser@test.com");
        dto.setFirstName("Panos");
        dto.setLastName("Batsos");
        dto.setPassword("Secret123!");
        dto.setIdCardNumber("AN123456");
        dto.setTaxNumber("123456789");

        when(userRepository.existsByEmail("newuser@test.com")).thenReturn(false);
        when(userRepository.existsByTaxNumber("123456789")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class , () -> {
            authenticationService.register(dto);
        });

        verify(userRepository , never()).save(any());
    }

    @Test
    void register_ShouldThrowException_WhenIdCardNumExists() {
        UserRegisterRequestDTO dto = new UserRegisterRequestDTO();
        dto.setEmail("newuser@test.com");
        dto.setFirstName("Panos");
        dto.setLastName("Batsos");
        dto.setPassword("Secret123!");
        dto.setIdCardNumber("AN123456");
        dto.setTaxNumber("123456789");

        when(userRepository.existsByEmail("newuser@test.com")).thenReturn(false);
        when(userRepository.existsByIdCardNumber("AN123456")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class , () -> {
            authenticationService.register(dto);
        });

        verify(userRepository , never()).save(any());
    }

    @Test
    void authenticate_ShouldThrowException_WhenCredentialsAreInvalid() {

        UserLoginRequestDTO loginDto = new UserLoginRequestDTO();
        loginDto.setEmail("wrong@test.com");
        loginDto.setPassword("wrongPassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> {
            authenticationService.login(loginDto);
        });

        verify(jwtService, never()).generateToken(any());
        verify(refreshTokenService, never()).createRefreshToken(anyString());
    }


    @Test
    void authenticate_ShouldThrowException_WhenPasswordIsIncorrect() {

        UserLoginRequestDTO loginDto = new UserLoginRequestDTO();
        loginDto.setEmail("user@test.com");
        loginDto.setPassword("wrong-password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid password"));


        assertThrows(BadCredentialsException.class, () -> {
            authenticationService.login(loginDto);
        });

        verify(jwtService, never()).generateToken(any());
        verify(refreshTokenService, never()).createRefreshToken(anyString());
    }

    @Test
    void authenticate_ShouldReturnTokens_WhenCredentialsAreValid() {

        UserLoginRequestDTO loginDto = new UserLoginRequestDTO();
        loginDto.setEmail("user@test.com");
        loginDto.setPassword("correctPassword");

        User mockUser = new User();
        mockUser.setEmail("user@test.com");
        mockUser.setFirstName("Panos");

        when(authenticationManager.authenticate(any())).thenReturn(null);

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(mockUser));

        when(jwtService.generateToken(mockUser)).thenReturn("real-jwt-token");

        RefreshToken mockRefreshToken = new RefreshToken();

        mockRefreshToken.setToken("real-refresh-token");
        when(refreshTokenService.createRefreshToken("user@test.com")).thenReturn(mockRefreshToken);

        AuthenticationResponse authenticationResponse = authenticationService.login(loginDto);
        Assertions.assertEquals("real-jwt-token" , authenticationResponse.getToken());
        Assertions.assertEquals("real-refresh-token" , authenticationResponse.getRefreshToken());

    }


}
