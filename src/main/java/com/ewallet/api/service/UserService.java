package com.ewallet.api.service;

import jakarta.transaction.Transactional;

import java.math.BigDecimal;


import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ewallet.api.dto.user.UserMapper;
import com.ewallet.api.dto.user.UserRegisterRequestDTO;
import com.ewallet.api.dto.user.UserResponseDTO;
import com.ewallet.api.entity.User;
import com.ewallet.api.entity.Wallet;
import com.ewallet.api.repository.UserRepository;


import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor // For automatic DI
public class UserService {
    
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * Registers a new user and automatically initializes their e-wallet
     * @param dto The data transfer object containing reg details(firstname , lastname , email 
     * , password , currency , dob , id card's number , taxnumber)
     * @return UserResponseDTO 
     * @throws RuntimeException (for now) if the provided email is already registered in the db 
     */

    @Transactional // Ensures that the user and wallet creation happen as a single atomic operation
    public UserResponseDTO registerUser(UserRegisterRequestDTO dto){
        // Check if the email is already in use
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("This user already exist");
        }

        // Mapping and encoding the password
        User user = userMapper.toUser(dto);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        
        // Build the wallet for the user  
        Wallet wallet = Wallet.builder()
        .balance(BigDecimal.ZERO)
        .currency(dto.getCurrency())
        .user(user)
        .build();


        user.setWallet(wallet);
        
        // Save the user (wallet is saved automatically due to cascade)
        User savedUser = userRepository.save(user);

        // Response
        return userMapper.toDTO(savedUser);
    }
}

