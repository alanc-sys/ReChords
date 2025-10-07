package com.misacordes.application.dto.response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthResponseTest {

    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        authResponse = AuthResponse.builder()
                .token("jwt.token.here")
                .build();
    }

    @Test
    void testAuthResponseBuilder() {
        // Assert
        assertEquals("jwt.token.here", authResponse.getToken());
    }

    @Test
    void testAuthResponseNoArgsConstructor() {
        // Act
        AuthResponse emptyResponse = new AuthResponse();

        // Assert
        assertNotNull(emptyResponse);
        assertNull(emptyResponse.getToken());
    }

    @Test
    void testAuthResponseAllArgsConstructor() {
        // Act
        AuthResponse newResponse = new AuthResponse("new.jwt.token");

        // Assert
        assertEquals("new.jwt.token", newResponse.getToken());
    }

    @Test
    void testAuthResponseSetters() {
        // Arrange
        AuthResponse response = new AuthResponse();

        // Act
        response.setToken("setter.jwt.token");

        // Assert
        assertEquals("setter.jwt.token", response.getToken());
    }

    @Test
    void testAuthResponseEquality() {
        // Arrange
        AuthResponse response1 = AuthResponse.builder()
                .token("jwt.token.here")
                .build();

        AuthResponse response2 = AuthResponse.builder()
                .token("jwt.token.here")
                .build();

        // Act & Assert
        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
    }

    @Test
    void testAuthResponseInequality() {
        // Arrange
        AuthResponse response1 = AuthResponse.builder()
                .token("token1")
                .build();

        AuthResponse response2 = AuthResponse.builder()
                .token("token2")
                .build();

        // Act & Assert
        assertNotEquals(response1, response2);
    }

    @Test
    void testAuthResponseToString() {
        // Act
        String toString = authResponse.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("AuthResponse"));
        assertTrue(toString.contains("jwt.token.here"));
    }

    @Test
    void testAuthResponseWithNullToken() {
        // Arrange
        AuthResponse responseWithNull = AuthResponse.builder()
                .token(null)
                .build();

        // Act & Assert
        assertNull(responseWithNull.getToken());
    }

    @Test
    void testAuthResponseWithEmptyToken() {
        // Arrange
        AuthResponse responseWithEmpty = AuthResponse.builder()
                .token("")
                .build();

        // Act & Assert
        assertEquals("", responseWithEmpty.getToken());
    }

    @Test
    void testAuthResponseWithLongToken() {
        // Arrange
        String longToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        
        AuthResponse responseWithLongToken = AuthResponse.builder()
                .token(longToken)
                .build();

        // Act & Assert
        assertEquals(longToken, responseWithLongToken.getToken());
        assertTrue(responseWithLongToken.getToken().length() > 100);
    }
}
