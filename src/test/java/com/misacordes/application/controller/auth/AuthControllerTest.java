package com.misacordes.application.controller.auth;

import com.misacordes.application.dto.request.LoginRequest;
import com.misacordes.application.dto.request.RegisterRequest;
import com.misacordes.application.dto.response.AuthResponse;
import com.misacordes.application.services.auth.AuthService;
import com.misacordes.application.services.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;
    
    @Mock(lenient = true)
    private RateLimitService rateLimitService;
    
    @Mock(lenient = true)
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private AuthController authController;

    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
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

        authResponse = AuthResponse.builder()
                .token("jwt.token.here")
                .build();
        
        // Setup default mock behavior
        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(rateLimitService.isLoginAllowed(anyString())).thenReturn(true);
        when(rateLimitService.isRegistrationAllowed(anyString())).thenReturn(true);
    }

    @Test
    void testLogin_Success() {
        // Arrange
        when(authService.login(loginRequest)).thenReturn(authResponse);

        // Act
        ResponseEntity<?> response = authController.login(loginRequest, httpServletRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        verify(authService).login(loginRequest);
        verify(rateLimitService).isLoginAllowed(anyString());
    }

    @Test
    void testLogin_RateLimitExceeded() {
        // Arrange
        when(rateLimitService.isLoginAllowed(anyString())).thenReturn(false);

        // Act
        ResponseEntity<?> response = authController.login(loginRequest, httpServletRequest);

        // Assert
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        verify(authService, never()).login(any());
    }

    @Test
    void testRegister_Success() {
        // Arrange
        when(authService.register(registerRequest)).thenReturn(authResponse);

        // Act
        ResponseEntity<?> response = authController.register(registerRequest, httpServletRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        verify(authService).register(registerRequest);
        verify(rateLimitService).isRegistrationAllowed(anyString());
    }

    @Test
    void testRegister_RateLimitExceeded() {
        // Arrange
        when(rateLimitService.isRegistrationAllowed(anyString())).thenReturn(false);

        // Act
        ResponseEntity<?> response = authController.register(registerRequest, httpServletRequest);

        // Assert
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        verify(authService, never()).register(any());
    }

    @Test
    void testLogin_ServiceThrowsException() {
        // Arrange
        RuntimeException serviceException = new RuntimeException("Service error");
        when(authService.login(loginRequest)).thenThrow(serviceException);

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authController.login(loginRequest, httpServletRequest)
        );
        
        assertEquals("Service error", exception.getMessage());
        verify(authService).login(loginRequest);
    }

    @Test
    void testRegister_ServiceThrowsException() {
        // Arrange
        RuntimeException serviceException = new RuntimeException("Service error");
        when(authService.register(registerRequest)).thenThrow(serviceException);

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authController.register(registerRequest, httpServletRequest)
        );
        
        assertEquals("Service error", exception.getMessage());
        verify(authService).register(registerRequest);
    }

    @Test
    void testLogin_WithXForwardedForHeader() {
        // Arrange
        when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1");
        when(authService.login(loginRequest)).thenReturn(authResponse);

        // Act
        ResponseEntity<?> response = authController.login(loginRequest, httpServletRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(rateLimitService).isLoginAllowed("192.168.1.1");
    }
}
