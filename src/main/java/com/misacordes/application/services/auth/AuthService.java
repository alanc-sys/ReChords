package com.misacordes.application.services.auth;
import com.misacordes.application.dto.response.AuthResponse;
import com.misacordes.application.dto.request.LoginRequest;
import com.misacordes.application.dto.request.RegisterRequest;
import com.misacordes.application.entities.Role;
import com.misacordes.application.repositories.UserRepository;
import com.misacordes.application.services.PlaylistService;
import com.misacordes.application.services.LoginAttemptService;
import lombok.RequiredArgsConstructor;
import com.misacordes.application.entities.User;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final PlaylistService playlistService;
    private final LoginAttemptService loginAttemptService;

    public AuthResponse login(LoginRequest request) {
        // Verificar si la cuenta está bloqueada
        if (loginAttemptService.isAccountLocked(request.getUsername())) {
            long minutesRemaining = loginAttemptService.getLockTimeRemaining(request.getUsername());
            throw new LockedException(
                "Cuenta bloqueada por múltiples intentos fallidos. " +
                "Intenta nuevamente en " + minutesRemaining + " minutos."
            );
        }
        
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            
            // Login exitoso - resetear intentos fallidos
            loginAttemptService.loginSucceeded(request.getUsername());
            
            User user = userRepository.findByUsername(request.getUsername());
            if (user == null) {
                throw new UsernameNotFoundException("User not found");
            }
            
            String token = jwtService.getToken(user);
            return AuthResponse.builder()
                    .token(token)
                    .build();
                    
        } catch (BadCredentialsException e) {
            // Login fallido - incrementar contador
            loginAttemptService.loginFailed(request.getUsername());
            int attemptsRemaining = loginAttemptService.getAttemptsRemaining(request.getUsername());
            
            if (attemptsRemaining > 0) {
                throw new BadCredentialsException(
                    "Credenciales inválidas. Te quedan " + attemptsRemaining + " intentos."
                );
            } else {
                throw new LockedException(
                    "Cuenta bloqueada por múltiples intentos fallidos. " +
                    "Intenta nuevamente en 15 minutos."
                );
            }
        }
    }


    public AuthResponse register(RegisterRequest request) {
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .country(request.getCountry())
                .role(Role.USER)
                .failedAttempts(0)
                .accountLocked(false)
                .build();
        User savedUser = userRepository.save(user);
        
        // Crear playlists por defecto para el nuevo usuario
        playlistService.createDefaultPlaylistsForUser(savedUser);
        
        return AuthResponse.builder()
                .token(jwtService.getToken(savedUser))
                .build();
    }
}
