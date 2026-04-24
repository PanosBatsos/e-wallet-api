package com.ewallet.api.dto.user;

import org.springframework.stereotype.Component;

import com.ewallet.api.entity.User;

@Component
public class UserMapper {

    public User toUser(UserRegisterRequestDTO dto) {
        return User.builder()
            .email(dto.getEmail())
            .password(dto.getPassword()) 
            .firstName(dto.getFirstName())
            .lastName(dto.getLastName())
            .birthDate(dto.getBirthDate())
            .idCardNumber(dto.getIdCardNumber())
            .taxNumber(dto.getTaxNumber())
            .build();
    }


    public UserResponseDTO toDTO (User user) {
        if (user.getWallet() == null) {
            throw new RuntimeException("User has not been mapped to a wallet");
        }

        return UserResponseDTO.builder()
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .id(user.getId())
            .birthDate(user.getBirthDate())
            .walletId(user.getWallet().getId())
            .lastName(user.getLastName())
            .build();
    }
}
