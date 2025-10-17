package com.misacordes.application.config;

import com.misacordes.application.entities.Role;
import com.misacordes.application.entities.User;
import com.misacordes.application.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationConfigTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationConfiguration authenticationConfiguration;

    @InjectMocks
    private ApplicationConfig applicationConfig;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("password123")
                .firstname("Test")
                .lastname("User")
                .country("Spain")
                .role(Role.USER)
                .build();
    }

    @Test
    void testPasswordEncoder() {
        // Act
        PasswordEncoder passwordEncoder = applicationConfig.passwordEncoder();

        // Assert
        assertNotNull(passwordEncoder);
        assertTrue(passwordEncoder.matches("password123", passwordEncoder.encode("password123")));
        assertFalse(passwordEncoder.matches("wrongpassword", passwordEncoder.encode("password123")));
    }

    @Test
    void testUserDetailsService_UserExists() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(testUser);

        // Act
        UserDetailsService userDetailsService = applicationConfig.userDetailsService();
        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        // Assert
        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("password123", userDetails.getPassword());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
        assertTrue(userDetails.isEnabled());
        assertEquals(1, userDetails.getAuthorities().size());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("USER")));
    }

    @Test
    void testUserDetailsService_UserNotExists() {
        // Arrange
        when(userRepository.findByUsername("nonexistentuser")).thenReturn(null);

        // Act
        UserDetailsService userDetailsService = applicationConfig.userDetailsService();

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("nonexistentuser")
        );
        
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testUserDetailsService_WithNullUsername() {
        // Arrange
        when(userRepository.findByUsername(null)).thenReturn(null);

        // Act
        UserDetailsService userDetailsService = applicationConfig.userDetailsService();

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername(null)
        );
        
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testAuthenticationProvider() {
        // Act
        AuthenticationProvider authenticationProvider = applicationConfig.authenticationProvider();

        // Assert
        assertNotNull(authenticationProvider);
        assertTrue(authenticationProvider instanceof DaoAuthenticationProvider);
        
        // Verificar que el provider está configurado correctamente
        DaoAuthenticationProvider daoProvider = (DaoAuthenticationProvider) authenticationProvider;
        assertNotNull(daoProvider);
    }

    @Test
    void testAuthenticationManager() throws Exception {
        // Arrange
        AuthenticationManager mockAuthManager = mock(AuthenticationManager.class);
        when(authenticationConfiguration.getAuthenticationManager()).thenReturn(mockAuthManager);

        // Act
        AuthenticationManager result = applicationConfig.authenticationManager(authenticationConfiguration);

        // Assert
        assertNotNull(result);
        assertEquals(mockAuthManager, result);
        verify(authenticationConfiguration).getAuthenticationManager();
    }

    @Test
    void testPasswordEncoderConsistency() {
        // Act
        PasswordEncoder passwordEncoder = applicationConfig.passwordEncoder();
        String encodedPassword = passwordEncoder.encode("testpassword");

        // Assert
        assertNotNull(encodedPassword);
        assertNotEquals("testpassword", encodedPassword); // Should be encoded
        assertTrue(passwordEncoder.matches("testpassword", encodedPassword));
        assertFalse(passwordEncoder.matches("wrongpassword", encodedPassword));
    }

    @Test
    void testPasswordEncoderDifferentEncodings() {
        // Act
        PasswordEncoder passwordEncoder = applicationConfig.passwordEncoder();
        String encodedPassword1 = passwordEncoder.encode("testpassword");
        String encodedPassword2 = passwordEncoder.encode("testpassword");

        // Assert
        assertNotEquals(encodedPassword1, encodedPassword2); // Each encoding should be unique
        assertTrue(passwordEncoder.matches("testpassword", encodedPassword1));
        assertTrue(passwordEncoder.matches("testpassword", encodedPassword2));
    }

    @Test
    void testUserDetailsServiceWithAdminUser() {
        // Arrange
        User adminUser = User.builder()
                .id(2L)
                .username("adminuser")
                .password("adminpassword")
                .firstname("Admin")
                .lastname("User")
                .country("Spain")
                .role(Role.ADMIN)
                .build();
        
        when(userRepository.findByUsername("adminuser")).thenReturn(adminUser);

        // Act
        UserDetailsService userDetailsService = applicationConfig.userDetailsService();
        UserDetails userDetails = userDetailsService.loadUserByUsername("adminuser");

        // Assert
        assertNotNull(userDetails);
        assertEquals("adminuser", userDetails.getUsername());
        assertEquals("adminpassword", userDetails.getPassword());
        assertEquals(1, userDetails.getAuthorities().size());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ADMIN")));
    }
}
