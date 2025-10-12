package com.misacordes.application.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
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
    void testUserBuilder() {
        // Assert
        assertEquals(1L, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("password123", user.getPassword());
        assertEquals("Test", user.getFirstname());
        assertEquals("User", user.getLastname());
        assertEquals("Spain", user.getCountry());
        assertEquals(Role.USER, user.getRole());
    }

    @Test
    void testUserNoArgsConstructor() {
        // Act
        User emptyUser = new User();

        // Assert
        assertNotNull(emptyUser);
        assertEquals(0L, emptyUser.getId());
        assertNull(emptyUser.getUsername());
        assertNull(emptyUser.getPassword());
        assertNull(emptyUser.getFirstname());
        assertNull(emptyUser.getLastname());
        assertNull(emptyUser.getCountry());
        assertNull(emptyUser.getRole());
    }

    @Test
    void testUserAllArgsConstructor() {
        // Act
        User newUser = new User(2L, "newuser", "pass", "New", "User", "France", Role.ADMIN, 0, false, null);

        // Assert
        assertEquals(2L, newUser.getId());
        assertEquals("newuser", newUser.getUsername());
        assertEquals("pass", newUser.getPassword());
        assertEquals("New", newUser.getFirstname());
        assertEquals("User", newUser.getLastname());
        assertEquals("France", newUser.getCountry());
        assertEquals(Role.ADMIN, newUser.getRole());
        assertEquals(0, newUser.getFailedAttempts());
        assertEquals(false, newUser.getAccountLocked());
        assertNull(newUser.getLockTime());
    }

    @Test
    void testGetAuthorities_UserRole() {
        // Act
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        // Assert
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority("USER")));
    }

    @Test
    void testGetAuthorities_AdminRole() {
        // Arrange
        user.setRole(Role.ADMIN);

        // Act
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        // Assert
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ADMIN")));
    }

    @Test
    void testGetAuthorities_NullRole() {
        // Arrange
        user.setRole(null);

        // Act & Assert
        assertThrows(NullPointerException.class, () -> user.getAuthorities());
    }

    @Test
    void testIsAccountNonExpired() {
        // Act & Assert
        assertTrue(user.isAccountNonExpired());
    }

    @Test
    void testIsAccountNonLocked() {
        // Act & Assert
        assertTrue(user.isAccountNonLocked());
    }
    
    @Test
    void testIsAccountNonLocked_WhenLocked() {
        // Arrange
        user.setAccountLocked(true);
        user.setLockTime(java.time.LocalDateTime.now());
        
        // Act & Assert
        assertFalse(user.isAccountNonLocked());
    }
    
    @Test
    void testIsAccountNonLocked_WhenLockExpired() {
        // Arrange - lockTime hace más de 15 minutos
        user.setAccountLocked(true);
        user.setLockTime(java.time.LocalDateTime.now().minusMinutes(20));
        
        // Act & Assert
        // Debería estar desbloqueado (pero el servicio debe actualizar la BD)
        assertTrue(user.isAccountNonLocked());
    }
    
    @Test
    void testFailedAttemptsHandling() {
        // Arrange & Act
        user.setFailedAttempts(3);
        
        // Assert
        assertEquals(3, user.getFailedAttempts());
        assertNotNull(user.getFailedAttempts());
    }
    
    @Test
    void testLockTimeHandling() {
        // Arrange
        java.time.LocalDateTime lockTime = java.time.LocalDateTime.now();
        
        // Act
        user.setLockTime(lockTime);
        
        // Assert
        assertEquals(lockTime, user.getLockTime());
    }

    @Test
    void testIsCredentialsNonExpired() {
        // Act & Assert
        assertTrue(user.isCredentialsNonExpired());
    }

    @Test
    void testIsEnabled() {
        // Act & Assert
        assertTrue(user.isEnabled());
    }

    @Test
    void testUserEquality() {
        // Arrange
        User user1 = User.builder()
                .id(1L)
                .username("testuser")
                .password("password123")
                .firstname("Test")
                .lastname("User")
                .country("Spain")
                .role(Role.USER)
                .build();

        User user2 = User.builder()
                .id(1L)
                .username("testuser")
                .password("password123")
                .firstname("Test")
                .lastname("User")
                .country("Spain")
                .role(Role.USER)
                .build();

        // Act & Assert
        assertEquals(user1, user2);
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    void testUserInequality() {
        // Arrange
        User user1 = User.builder()
                .id(1L)
                .username("testuser")
                .role(Role.USER)
                .build();

        User user2 = User.builder()
                .id(2L)
                .username("otheruser")
                .role(Role.ADMIN)
                .build();

        // Act & Assert
        assertNotEquals(user1, user2);
    }

    @Test
    void testUserToString() {
        // Act
        String toString = user.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("User"));
        assertTrue(toString.contains("testuser"));
    }

    @Test
    void testUserWithNullFields() {
        // Arrange
        User userWithNulls = User.builder()
                .id(1L)
                .username("testuser")
                .password("password")
                .firstname(null)
                .lastname(null)
                .country(null)
                .role(Role.USER)
                .build();

        // Act & Assert
        assertNull(userWithNulls.getFirstname());
        assertNull(userWithNulls.getLastname());
        assertNull(userWithNulls.getCountry());
        assertNotNull(userWithNulls.getRole());
        assertTrue(userWithNulls.isAccountNonExpired());
        assertTrue(userWithNulls.isAccountNonLocked());
        assertTrue(userWithNulls.isCredentialsNonExpired());
        assertTrue(userWithNulls.isEnabled());
    }

    @Test
    void testUserSetters() {
        // Arrange
        User newUser = new User();

        // Act
        newUser.setId(5L);
        newUser.setUsername("setteruser");
        newUser.setPassword("newpassword");
        newUser.setFirstname("Setter");
        newUser.setLastname("User");
        newUser.setCountry("Italy");
        newUser.setRole(Role.ADMIN);

        // Assert
        assertEquals(5L, newUser.getId());
        assertEquals("setteruser", newUser.getUsername());
        assertEquals("newpassword", newUser.getPassword());
        assertEquals("Setter", newUser.getFirstname());
        assertEquals("User", newUser.getLastname());
        assertEquals("Italy", newUser.getCountry());
        assertEquals(Role.ADMIN, newUser.getRole());
    }
}
