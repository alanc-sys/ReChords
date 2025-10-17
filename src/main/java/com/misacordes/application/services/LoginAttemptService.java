package com.misacordes.application.services;

import com.misacordes.application.entities.User;
import com.misacordes.application.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginAttemptService {

    private final UserRepository userRepository;
    
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_TIME_DURATION_MINUTES = 15;

    @Transactional
    public void loginSucceeded(String username) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            if (user.getFailedAttempts() != null && user.getFailedAttempts() > 0) {
                user.setFailedAttempts(0);
                user.setAccountLocked(false);
                user.setLockTime(null);
                userRepository.save(user);
                log.info("Login exitoso para usuario: {}. Intentos fallidos reseteados.", username);
            }
        }
    }

    @Transactional
    public void loginFailed(String username) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            int attempts = (user.getFailedAttempts() != null) ? user.getFailedAttempts() : 0;
            attempts++;
            user.setFailedAttempts(attempts);
            
            if (attempts >= MAX_FAILED_ATTEMPTS) {
                user.setAccountLocked(true);
                user.setLockTime(LocalDateTime.now());
                log.warn("Cuenta bloqueada para usuario: {} después de {} intentos fallidos", 
                         username, attempts);
            } else {
                log.warn("Intento de login fallido para usuario: {}. Intentos: {}/{}", 
                         username, attempts, MAX_FAILED_ATTEMPTS);
            }
            
            userRepository.save(user);
        }
    }

    public boolean isAccountLocked(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return false;
        }
        
        if (user.getAccountLocked() != null && user.getAccountLocked()) {
            // Verificar si ya pasó el tiempo de bloqueo
            if (user.getLockTime() != null) {
                LocalDateTime unlockTime = user.getLockTime().plusMinutes(LOCK_TIME_DURATION_MINUTES);
                if (LocalDateTime.now().isAfter(unlockTime)) {
                    // Desbloquear automáticamente
                    unlockAccount(username);
                    return false;
                }
            }
            return true;
        }
        
        return false;
    }

    @Transactional
    public void unlockAccount(String username) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            user.setAccountLocked(false);
            user.setFailedAttempts(0);
            user.setLockTime(null);
            userRepository.save(user);
            log.info("Cuenta desbloqueada para usuario: {}", username);
        }
    }

    public long getLockTimeRemaining(String username) {
        User user = userRepository.findByUsername(username);
        if (user != null && user.getAccountLocked() != null && user.getAccountLocked()) {
            if (user.getLockTime() != null) {
                LocalDateTime unlockTime = user.getLockTime().plusMinutes(LOCK_TIME_DURATION_MINUTES);
                long minutesRemaining = java.time.Duration.between(LocalDateTime.now(), unlockTime).toMinutes();
                return Math.max(0, minutesRemaining);
            }
        }
        return 0;
    }
    public int getAttemptsRemaining(String username) {
        User user = userRepository.findByUsername(username);
        if (user != null && user.getFailedAttempts() != null) {
            return Math.max(0, MAX_FAILED_ATTEMPTS - user.getFailedAttempts());
        }
        return MAX_FAILED_ATTEMPTS;
    }
}

