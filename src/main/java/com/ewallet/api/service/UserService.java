package com.ewallet.api.service;

import com.ewallet.api.exception.UserAlreadyExistsException;
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
    
   /*Empty for now*/
}

