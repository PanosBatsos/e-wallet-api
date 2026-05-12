package com.ewallet.api.security;

import com.ewallet.api.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

// Wrapper class that decouples our User entity from spring security's UserDetails
// and ensures that the core business logic remains independent of the security
@RequiredArgsConstructor
public class UserSecurity implements UserDetails {

    private final User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Spring security requires the "ROLE_" prefix to correctly use hasRole() expressions
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getUserRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // Can be linked to WalletStatus.LOCKED in the future if needed
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    public User getUser() {
        return user;
    }
}
