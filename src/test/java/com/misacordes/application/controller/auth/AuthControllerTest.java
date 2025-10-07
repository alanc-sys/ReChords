package com.misacordes.application.controller.auth;

import com.misacordes.application.dto.request.LoginRequest;
import com.misacordes.application.dto.request.RegisterRequest;
import com.misacordes.application.dto.response.AuthResponse;
import com.misacordes.application.services.auth.AuthService;
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
    }

    @Test
    void testLogin_Success() {
        // Arrange
        when(authService.login(loginRequest)).thenReturn(authResponse);

        // Act
        ResponseEntity<AuthResponse> response = authController.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(authResponse.getToken(), response.getBody().getToken());
        
        verify(authService).login(loginRequest);
    }

    @Test
    void testLogin_WithNullRequest() {
        // Act & Assert - El controlador no valida null, simplemente pasa el null al servicio
        assertDoesNotThrow(() -> authController.login(null));
    }

    @Test
    void testRegister_Success() {
        // Arrange
        when(authService.register(registerRequest)).thenReturn(authResponse);

        // Act
        ResponseEntity<AuthResponse> response = authController.register(registerRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(authResponse.getToken(), response.getBody().getToken());
        
        verify(authService).register(registerRequest);
    }

    @Test
    void testRegister_WithNullRequest() {
        // Act & Assert - El controlador no valida null, simplemente pasa el null al servicio
        assertDoesNotThrow(() -> authController.register(null));
    }

    @Test
    void testLogin_ServiceThrowsException() {
        // Arrange
        RuntimeException serviceException = new RuntimeException("Service error");
        when(authService.login(loginRequest)).thenThrow(serviceException);

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authController.login(loginRequest)
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
                () -> authController.register(registerRequest)
        );
        
        assertEquals("Service error", exception.getMessage());
        verify(authService).register(registerRequest);
    }

    @Test
    void testLogin_WithEmptyUsername() {
        // Arrange
        LoginRequest emptyUsernameRequest = LoginRequest.builder()
                .username("")
                .password("password123")
                .build();
        
        when(authService.login(emptyUsernameRequest)).thenReturn(authResponse);

        // Act
        ResponseEntity<AuthResponse> response = authController.login(emptyUsernameRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(authService).login(emptyUsernameRequest);
    }

    @Test
    void testRegister_WithMinimalData() {
        // Arrange
        RegisterRequest minimalRequest = RegisterRequest.builder()
                .username("user")
                .password("pass")
                .build();
        
        when(authService.register(minimalRequest)).thenReturn(authResponse);

        // Act
        ResponseEntity<AuthResponse> response = authController.register(minimalRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(authService).register(minimalRequest);
    }
}
