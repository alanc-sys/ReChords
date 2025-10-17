package com.misacordes.application.dto.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegisterRequestTest {

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .username("testuser")
                .password("password123")
                .firstname("Test")
                .lastname("User")
                .country("Spain")
                .build();
    }

    @Test
    void testRegisterRequestBuilder() {
        // Assert
        assertEquals("testuser", registerRequest.getUsername());
        assertEquals("password123", registerRequest.getPassword());
        assertEquals("Test", registerRequest.getFirstname());
        assertEquals("User", registerRequest.getLastname());
        assertEquals("Spain", registerRequest.getCountry());
    }

    @Test
    void testRegisterRequestNoArgsConstructor() {
        // Act
        RegisterRequest emptyRequest = new RegisterRequest();

        // Assert
        assertNotNull(emptyRequest);
        assertNull(emptyRequest.getUsername());
        assertNull(emptyRequest.getPassword());
        assertNull(emptyRequest.getFirstname());
        assertNull(emptyRequest.getLastname());
        assertNull(emptyRequest.getCountry());
    }

    @Test
    void testRegisterRequestAllArgsConstructor() {
        // Act
        RegisterRequest newRequest = new RegisterRequest("newuser", "newpassword", "New", "User", "France");

        // Assert
        assertEquals("newuser", newRequest.getUsername());
        assertEquals("newpassword", newRequest.getPassword());
        assertEquals("New", newRequest.getFirstname());
        assertEquals("User", newRequest.getLastname());
        assertEquals("France", newRequest.getCountry());
    }

    @Test
    void testRegisterRequestSetters() {
        // Arrange
        RegisterRequest request = new RegisterRequest();

        // Act
        request.setUsername("setteruser");
        request.setPassword("setterpassword");
        request.setFirstname("Setter");
        request.setLastname("User");
        request.setCountry("Italy");

        // Assert
        assertEquals("setteruser", request.getUsername());
        assertEquals("setterpassword", request.getPassword());
        assertEquals("Setter", request.getFirstname());
        assertEquals("User", request.getLastname());
        assertEquals("Italy", request.getCountry());
    }

    @Test
    void testRegisterRequestEquality() {
        // Arrange
        RegisterRequest request1 = RegisterRequest.builder()
                .username("testuser")
                .password("password123")
                .firstname("Test")
                .lastname("User")
                .country("Spain")
                .build();

        RegisterRequest request2 = RegisterRequest.builder()
                .username("testuser")
                .password("password123")
                .firstname("Test")
                .lastname("User")
                .country("Spain")
                .build();

        // Act & Assert
        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
    }

    @Test
    void testRegisterRequestInequality() {
        // Arrange
        RegisterRequest request1 = RegisterRequest.builder()
                .username("user1")
                .password("password1")
                .firstname("User")
                .lastname("One")
                .country("Spain")
                .build();

        RegisterRequest request2 = RegisterRequest.builder()
                .username("user2")
                .password("password2")
                .firstname("User")
                .lastname("Two")
                .country("France")
                .build();

        // Act & Assert
        assertNotEquals(request1, request2);
    }

    @Test
    void testRegisterRequestToString() {
        // Act
        String toString = registerRequest.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("RegisterRequest"));
        assertTrue(toString.contains("testuser"));
    }

    @Test
    void testRegisterRequestWithNullFields() {
        // Arrange
        RegisterRequest requestWithNulls = RegisterRequest.builder()
                .username(null)
                .password(null)
                .firstname(null)
                .lastname(null)
                .country(null)
                .build();

        // Act & Assert
        assertNull(requestWithNulls.getUsername());
        assertNull(requestWithNulls.getPassword());
        assertNull(requestWithNulls.getFirstname());
        assertNull(requestWithNulls.getLastname());
        assertNull(requestWithNulls.getCountry());
    }

    @Test
    void testRegisterRequestWithEmptyStrings() {
        // Arrange
        RegisterRequest requestWithEmpties = RegisterRequest.builder()
                .username("")
                .password("")
                .firstname("")
                .lastname("")
                .country("")
                .build();

        // Act & Assert
        assertEquals("", requestWithEmpties.getUsername());
        assertEquals("", requestWithEmpties.getPassword());
        assertEquals("", requestWithEmpties.getFirstname());
        assertEquals("", requestWithEmpties.getLastname());
        assertEquals("", requestWithEmpties.getCountry());
    }

    @Test
    void testRegisterRequestWithMinimalData() {
        // Arrange
        RegisterRequest minimalRequest = RegisterRequest.builder()
                .username("minimaluser")
                .password("password")
                .build();

        // Act & Assert
        assertEquals("minimaluser", minimalRequest.getUsername());
        assertEquals("password", minimalRequest.getPassword());
        assertNull(minimalRequest.getFirstname());
        assertNull(minimalRequest.getLastname());
        assertNull(minimalRequest.getCountry());
    }
}
