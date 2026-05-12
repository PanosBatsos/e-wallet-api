package com.ewallet.api.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true , nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false , name = "created_at" , updatable = false)
    private LocalDateTime createdAt;

    @Column(unique = true)
    private String idCardNumber;

    @Column(unique = true , length = 9)
    private String taxNumber;

    @Column(nullable = false)
    private LocalDate birthDate;   


    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Assign a default role to all users for now
        // A dedicated role field will be added to the entity later
        return List.of(new SimpleGrantedAuthority(userRole.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Αν επιστρέψεις false, ο χρήστης δεν θα μπορεί να μπει
    }

    @Override
    public boolean isAccountNonLocked() {
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

    @Enumerated(EnumType.STRING)
    private UserRole userRole = UserRole.USER;
}
