package com.ewallet.api.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ewallet.api.dto.user.UserRegisterRequestDTO;
import com.ewallet.api.dto.user.UserResponseDTO;
import com.ewallet.api.dto.user.mapper.UserMapper;
import com.ewallet.api.entity.User;
import com.ewallet.api.repository.UserRepository;


@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;

    @Mock 
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;


    @InjectMocks
    private UserService userService;

    private UserRegisterRequestDTO dto;
    private User user;

    
    @BeforeEach
    void setUp() {
        dto = new UserRegisterRequestDTO();
        dto.setEmail("test@test.com");
        dto.setPassword("12345");
        dto.setCurrency("USD");

        user = new User();
        user.setEmail("test@test.com");
    }

    @Test
    void registerUser_Success() {
        // Tells the mock repository to return an empty optional (simulating that the email is not taken) 
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Instruct the mock mapper to return our predefined user entity when it receives the dto 
        when(userMapper.toUser(dto)).thenReturn(user);
        
        // Simulates the password hashing 
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_password");
        
        // Simulates the database save operation returning the same user entity
        when(userRepository.save(any(User.class))).thenReturn(user);
        
        // Instructs the mapper to return a new ResponseDTO 
        when(userMapper.toDTO(any(User.class))).thenReturn(new UserResponseDTO());

        // We trigger the service and capture the response
        UserResponseDTO result = userService.registerUser(dto);

        // Ensures that the service did not return null
        assertNotNull(result , "The response should not be null on successful registration");
        
        // Verifies tha that the user's repo save method was called exactly once
        verify(userRepository , times(1)).save(any(User.class));
        
        // Confirms that the password encoder was used
        verify(passwordEncoder).encode(anyString());
    }
}
