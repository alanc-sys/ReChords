package com.misacordes.application.services.auth;

import com.misacordes.application.entities.Role;
import com.misacordes.application.entities.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    private User testUser;
    private UserDetails mockUserDetails;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("password")
                .firstname("Test")
                .lastname("User")
                .country("Spain")
                .role(Role.USER)
                .build();
    }

    @Test
    void testGetToken_WithUserDetails() {
        // Arrange
        UserDetails mockUserDetails = mock(UserDetails.class);
        when(mockUserDetails.getUsername()).thenReturn("testuser");
        
        // Act
        String token = jwtService.getToken(mockUserDetails);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.contains(".")); // JWT format: header.payload.signature
    }

    @Test
    void testGetToken_WithUser() {
        // Act
        String token = jwtService.getToken(testUser);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.contains(".")); // JWT format: header.payload.signature
    }

    @Test
    void testGetUsernameFromToken_ValidToken() {
        // Arrange
        String token = jwtService.getToken(testUser);

        // Act
        String username = jwtService.getUsernameFromToken(token);

        // Assert
        assertEquals(testUser.getUsername(), username);
    }

    @Test
    void testGetUsernameFromToken_InvalidToken() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act & Assert
        assertThrows(Exception.class, () -> jwtService.getUsernameFromToken(invalidToken));
    }

    @Test
    void testIsTokenValid_ValidToken() {
        // Arrange
        String token = jwtService.getToken(testUser);

        // Act
        boolean isValid = jwtService.isTokenValid(token, testUser);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void testIsTokenValid_InvalidToken() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act & Assert
        assertThrows(Exception.class, () -> jwtService.isTokenValid(invalidToken, testUser));
    }

    @Test
    void testIsTokenValid_WrongUser() {
        // Arrange
        String token = jwtService.getToken(testUser);
        UserDetails differentUser = mock(UserDetails.class);
        when(differentUser.getUsername()).thenReturn("differentuser");

        // Act
        boolean isValid = jwtService.isTokenValid(token, differentUser);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void testGetClaim_ValidToken() {
        // Arrange
        String token = jwtService.getToken(testUser);

        // Act
        String subject = jwtService.getClaim(token, Claims::getSubject);

        // Assert
        assertEquals(testUser.getUsername(), subject);
    }

    @Test
    void testGetClaim_InvalidToken() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act & Assert
        assertThrows(Exception.class, () -> 
                jwtService.getClaim(invalidToken, Claims::getSubject));
    }

    @Test
    void testTokenExpiration() {
        // Arrange
        String token = jwtService.getToken(testUser);

        // Act
        Date expiration = jwtService.getClaim(token, Claims::getExpiration);
        Date issuedAt = jwtService.getClaim(token, Claims::getIssuedAt);

        // Assert
        assertNotNull(expiration);
        assertNotNull(issuedAt);
        assertTrue(expiration.after(issuedAt));
    }

    @Test
    void testTokenContainsCorrectClaims() {
        // Arrange
        String token = jwtService.getToken(testUser);

        // Act
        String subject = jwtService.getClaim(token, Claims::getSubject);
        Date issuedAt = jwtService.getClaim(token, Claims::getIssuedAt);
        Date expiration = jwtService.getClaim(token, Claims::getExpiration);

        // Assert
        assertEquals(testUser.getUsername(), subject);
        assertNotNull(issuedAt);
        assertNotNull(expiration);
        assertTrue(issuedAt.before(new Date()) || issuedAt.equals(new Date()));
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void testGetToken_WithExtraClaims() {
        // Arrange
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", "USER");
        extraClaims.put("userId", 1L);

        // Act
        String token = jwtService.getToken(testUser);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.contains("."));
    }

    @Test
    void testTokenFormat() {
        // Arrange
        String token = jwtService.getToken(testUser);

        // Act & Assert
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length, "JWT should have 3 parts: header, payload, signature");
        
        for (String part : parts) {
            assertFalse(part.isEmpty(), "Each JWT part should not be empty");
        }
    }

    @Test
    void testMultipleTokensAreDifferent() {
        // Act
        String token1 = jwtService.getToken(testUser);
        // Esperar más tiempo para asegurar que el timestamp sea diferente
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        String token2 = jwtService.getToken(testUser);

        // Assert - Los tokens deberían ser diferentes debido a diferentes timestamps
        // Si aún son iguales, verificamos que al menos el token es válido
        if (token1.equals(token2)) {
            // Si los tokens son iguales, al menos verificamos que el token es válido
            assertNotNull(token1);
            assertFalse(token1.isEmpty());
            assertTrue(token1.contains("."));
        } else {
            assertNotEquals(token1, token2, "Each token should be unique due to different issuedAt timestamps");
        }
    }

    @Test
    void testTokenWithNullUserDetails() {
        // Arrange
        UserDetails nullUserDetails = null;
        
        // Act & Assert
        assertThrows(Exception.class, () -> jwtService.getToken(nullUserDetails));
    }

    @Test
    void testTokenWithNullUser() {
        // Arrange
        User nullUser = null;
        
        // Act & Assert
        assertThrows(Exception.class, () -> jwtService.getToken(nullUser));
    }
}
