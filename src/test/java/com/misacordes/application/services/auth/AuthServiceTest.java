package com.misacordes.application.services.auth;

import com.misacordes.application.dto.request.LoginRequest;
import com.misacordes.application.dto.request.RegisterRequest;
import com.misacordes.application.dto.response.AuthResponse;
import com.misacordes.application.entities.Role;
import com.misacordes.application.entities.User;
import com.misacordes.application.repositories.UserRepository;
import com.misacordes.application.services.PlaylistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PlaylistService playlistService;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("encodedPassword")
                .firstname("Test")
                .lastname("User")
                .country("Spain")
                .role(Role.USER)
                .build();

        loginRequest = LoginRequest.builder()
                .username("testuser")
                .password("password123")
                .build();

        registerRequest = RegisterRequest.builder()
                .username("newuser")
                .password("password123")
                .firstname("New")
                .lastname("User")
                .country("Spain")
                .build();
    }

    @Test
    void testLogin_Success() {
        // Arrange
        String expectedToken = "jwt.token.here";
        Authentication mockAuth = mock(Authentication.class);
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuth);
        when(userRepository.findByUsername(loginRequest.getUsername()))
                .thenReturn(testUser);
        when(jwtService.getToken(testUser))
                .thenReturn(expectedToken);

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals(expectedToken, response.getToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByUsername(loginRequest.getUsername());
        verify(jwtService).getToken(testUser);
    }

    @Test
    void testLogin_UserNotFound() {
        // Arrange
        Authentication mockAuth = mock(Authentication.class);
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuth);
        when(userRepository.findByUsername(loginRequest.getUsername()))
                .thenReturn(null);

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> authService.login(loginRequest)
        );
        
        assertEquals("User not found", exception.getMessage());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByUsername(loginRequest.getUsername());
        verify(jwtService, never()).getToken(any());
    }

    @Test
    void testRegister_Success() {
        // Arrange
        String expectedToken = "jwt.token.here";
        String encodedPassword = "encodedPassword123";
        
        when(passwordEncoder.encode(registerRequest.getPassword()))
                .thenReturn(encodedPassword);
        when(userRepository.save(any(User.class)))
                .thenReturn(testUser);
        when(jwtService.getToken(any(User.class)))
                .thenReturn(expectedToken);

        // Act
        AuthResponse response = authService.register(registerRequest);

        // Assert
        assertNotNull(response);
        assertEquals(expectedToken, response.getToken());
        
        verify(passwordEncoder).encode(registerRequest.getPassword());
        verify(userRepository).save(argThat(user -> 
                user.getUsername().equals(registerRequest.getUsername()) &&
                user.getPassword().equals(encodedPassword) &&
                user.getFirstname().equals(registerRequest.getFirstname()) &&
                user.getLastname().equals(registerRequest.getLastname()) &&
                user.getCountry().equals(registerRequest.getCountry()) &&
                user.getRole().equals(Role.USER)
        ));
        verify(jwtService).getToken(any(User.class));
    }

    @Test
    void testRegister_WithNullFields() {
        // Arrange
        RegisterRequest requestWithNulls = RegisterRequest.builder()
                .username("user")
                .password("pass")
                .firstname(null)
                .lastname(null)
                .country(null)
                .build();
        
        String expectedToken = "jwt.token.here";
        String encodedPassword = "encodedPassword123";
        
        when(passwordEncoder.encode(requestWithNulls.getPassword()))
                .thenReturn(encodedPassword);
        when(userRepository.save(any(User.class)))
                .thenReturn(testUser);
        when(jwtService.getToken(any(User.class)))
                .thenReturn(expectedToken);

        // Act
        AuthResponse response = authService.register(requestWithNulls);

        // Assert
        assertNotNull(response);
        assertEquals(expectedToken, response.getToken());
        
        verify(userRepository).save(argThat(user -> 
                user.getUsername().equals(requestWithNulls.getUsername()) &&
                user.getPassword().equals(encodedPassword) &&
                user.getFirstname() == null &&
                user.getLastname() == null &&
                user.getCountry() == null &&
                user.getRole().equals(Role.USER)
        ));
    }
}
