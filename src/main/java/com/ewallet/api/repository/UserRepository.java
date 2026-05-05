package com.ewallet.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ewallet.api.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User , Long>{
    Optional<User> findByEmail(String email); // checking if this user already exists

    boolean existsByEmail(String email);
    boolean existsByTaxNumber(String taxNumber);
    boolean existsByIdCardNumber(String idCardNumber);

    Object findByTaxNumber(String taxNumber);
}
