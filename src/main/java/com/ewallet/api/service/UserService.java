package com.ewallet.api.service;

import jakarta.transaction.Transactional;

import java.math.BigDecimal;


import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ewallet.api.dto.user.UserRegisterRequestDTO;
import com.ewallet.api.dto.user.UserResponseDTO;
import com.ewallet.api.dto.user.mapper.UserMapper;
import com.ewallet.api.entity.User;
import com.ewallet.api.entity.Wallet;
import com.ewallet.api.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    
    @Transactional
    public UserResponseDTO registerUser(UserRegisterRequestDTO dto){
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("This user already exist");
        }

        User user = userMapper.toUser(dto);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
    
        Wallet wallet = Wallet.builder()
        .balance(BigDecimal.ZERO)
        .currency(dto.getCurrency())
        .user(user)
        .build();


        user.setWallet(wallet);
        
        User savedUser = userRepository.save(user);

        return userMapper.toDTO(savedUser);
    }
}

