package com.misacordes.application.dto.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginRequestTest {

    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        loginRequest = LoginRequest.builder()
                .username("testuser")
                .password("password123")
                .build();
    }

    @Test
    void testLoginRequestBuilder() {
        // Assert
        assertEquals("testuser", loginRequest.getUsername());
        assertEquals("password123", loginRequest.getPassword());
    }

    @Test
    void testLoginRequestNoArgsConstructor() {
        // Act
        LoginRequest emptyRequest = new LoginRequest();

        // Assert
        assertNotNull(emptyRequest);
        assertNull(emptyRequest.getUsername());
        assertNull(emptyRequest.getPassword());
    }

    @Test
    void testLoginRequestAllArgsConstructor() {
        // Act
        LoginRequest newRequest = new LoginRequest("newuser", "newpassword");

        // Assert
        assertEquals("newuser", newRequest.getUsername());
        assertEquals("newpassword", newRequest.getPassword());
    }

    @Test
    void testLoginRequestSetters() {
        // Arrange
        LoginRequest request = new LoginRequest();

        // Act
        request.setUsername("setteruser");
        request.setPassword("setterpassword");

        // Assert
        assertEquals("setteruser", request.getUsername());
        assertEquals("setterpassword", request.getPassword());
    }

    @Test
    void testLoginRequestEquality() {
        // Arrange
        LoginRequest request1 = LoginRequest.builder()
                .username("testuser")
                .password("password123")
                .build();

        LoginRequest request2 = LoginRequest.builder()
                .username("testuser")
                .password("password123")
                .build();

        // Act & Assert
        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
    }

    @Test
    void testLoginRequestInequality() {
        // Arrange
        LoginRequest request1 = LoginRequest.builder()
                .username("user1")
                .password("password1")
                .build();

        LoginRequest request2 = LoginRequest.builder()
                .username("user2")
                .password("password2")
                .build();

        // Act & Assert
        assertNotEquals(request1, request2);
    }

    @Test
    void testLoginRequestToString() {
        // Act
        String toString = loginRequest.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("LoginRequest"));
        assertTrue(toString.contains("testuser"));
    }

    @Test
    void testLoginRequestWithNullFields() {
        // Arrange
        LoginRequest requestWithNulls = LoginRequest.builder()
                .username(null)
                .password(null)
                .build();

        // Act & Assert
        assertNull(requestWithNulls.getUsername());
        assertNull(requestWithNulls.getPassword());
    }

    @Test
    void testLoginRequestWithEmptyStrings() {
        // Arrange
        LoginRequest requestWithEmpties = LoginRequest.builder()
                .username("")
                .password("")
                .build();

        // Act & Assert
        assertEquals("", requestWithEmpties.getUsername());
        assertEquals("", requestWithEmpties.getPassword());
    }
}
