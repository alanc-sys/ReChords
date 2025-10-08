package com.misacordes.application.services.auth;
import com.misacordes.application.dto.response.AuthResponse;
import com.misacordes.application.dto.request.LoginRequest;
import com.misacordes.application.dto.request.RegisterRequest;
import com.misacordes.application.entities.Role;
import com.misacordes.application.repositories.UserRepository;
import com.misacordes.application.services.PlaylistService;
import lombok.RequiredArgsConstructor;
import com.misacordes.application.entities.User;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        User user = userRepository.findByUsername(request.getUsername());
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        String token = jwtService.getToken(user);
        return AuthResponse.builder()
                .token(token)
                .build();

    }


    public AuthResponse register(RegisterRequest request) {
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .country(request.getCountry())
                .role(Role.USER)
                .build();
        User savedUser = userRepository.save(user);
        
        // Crear playlists por defecto para el nuevo usuario
        playlistService.createDefaultPlaylistsForUser(savedUser);
        
        return AuthResponse.builder()
                .token(jwtService.getToken(savedUser))
                .build();
    }
}
