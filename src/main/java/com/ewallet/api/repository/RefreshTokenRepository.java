package com.ewallet.api.repository;

import com.ewallet.api.entity.RefreshToken;
import com.ewallet.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;


import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken (String token);

    @Modifying
    void deleteByUser(User user);

}
