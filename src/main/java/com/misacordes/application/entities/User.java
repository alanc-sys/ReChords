package com.misacordes.application.entities;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@EqualsAndHashCode
@ToString
@Table(name = "user", uniqueConstraints = {@UniqueConstraint(columnNames = {"username"})})
public class User implements UserDetails {
    @Id
    @GeneratedValue
    private long id;
    @Column(nullable = false)
    private String username;
    private String password;
    private String firstname;
    private String lastname;
    private String country;
    @Enumerated(EnumType.STRING)
    private Role role;
    
    @Column(name = "failed_attempts", nullable = false)
    private Integer failedAttempts = 0;
    
    @Column(name = "account_locked")
    private Boolean accountLocked = false;
    
    @Column(name = "lock_time")
    private java.time.LocalDateTime lockTime;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return role != null ? List.of(new SimpleGrantedAuthority(role.name())) : List.of();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        if (accountLocked != null && accountLocked) {
            if (lockTime != null) {
                if (java.time.LocalDateTime.now().isAfter(lockTime.plusMinutes(15))) {
                    return true; // El servicio desbloqueará la cuenta
                }
                return false;
            }
            return false;
        }
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
}
