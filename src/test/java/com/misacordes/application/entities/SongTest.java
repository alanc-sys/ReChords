package com.misacordes.application.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SongTest {

    private Song song;
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

        song = Song.builder()
                .id(1L)
                .title("Test Song")
                .artist("Test Artist")
                .album("Test Album")
                .year(2023)
                .user(testUser)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testSongBuilder() {
        // Assert
        assertEquals(1L, song.getId());
        assertEquals("Test Song", song.getTitle());
        assertEquals("Test Artist", song.getArtist());
        assertEquals("Test Album", song.getAlbum());
        assertEquals(2023, song.getYear());
        assertEquals(testUser, song.getUser());
        assertNotNull(song.getCreatedAt());
    }

    @Test
    void testSongNoArgsConstructor() {
        // Act
        Song emptySong = new Song();

        // Assert
        assertNotNull(emptySong);
        assertEquals(0L, emptySong.getId());
        assertNull(emptySong.getTitle());
        assertNull(emptySong.getArtist());
        assertNull(emptySong.getAlbum());
        assertNull(emptySong.getYear());
        assertNull(emptySong.getUser());
        assertNull(emptySong.getCreatedAt());
    }

    @Test
    void testSongAllArgsConstructor() {
        // Arrange
        LocalDateTime testTime = LocalDateTime.of(2023, 1, 1, 12, 0);
        User newUser = User.builder()
                .id(2L)
                .username("newuser")
                .role(Role.USER)
                .build();

        // Act
        Song newSong = new Song(2L, "New Song", "New Artist", "New Album", 2024, newUser, testTime);

        // Assert
        assertEquals(2L, newSong.getId());
        assertEquals("New Song", newSong.getTitle());
        assertEquals("New Artist", newSong.getArtist());
        assertEquals("New Album", newSong.getAlbum());
        assertEquals(2024, newSong.getYear());
        assertEquals(newUser, newSong.getUser());
        assertEquals(testTime, newSong.getCreatedAt());
    }

    @Test
    void testOnCreated() {
        // Arrange
        Song newSong = new Song();
        LocalDateTime beforeCreation = LocalDateTime.now();

        // Act
        newSong.onCreated();

        // Assert
        assertNotNull(newSong.getCreatedAt());
        assertTrue(newSong.getCreatedAt().isAfter(beforeCreation.minusSeconds(1)));
        assertTrue(newSong.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void testSongEquality() {
        // Arrange
        Song song1 = Song.builder()
                .id(1L)
                .title("Test Song")
                .artist("Test Artist")
                .album("Test Album")
                .year(2023)
                .user(testUser)
                .createdAt(LocalDateTime.now())
                .build();

        Song song2 = Song.builder()
                .id(1L)
                .title("Test Song")
                .artist("Test Artist")
                .album("Test Album")
                .year(2023)
                .user(testUser)
                .createdAt(LocalDateTime.now())
                .build();

        // Act & Assert
        assertEquals(song1, song2);
        assertEquals(song1.hashCode(), song2.hashCode());
    }

    @Test
    void testSongInequality() {
        // Arrange
        Song song1 = Song.builder()
                .id(1L)
                .title("Song 1")
                .artist("Artist 1")
                .build();

        Song song2 = Song.builder()
                .id(2L)
                .title("Song 2")
                .artist("Artist 2")
                .build();

        // Act & Assert
        assertNotEquals(song1, song2);
    }

    @Test
    void testSongToString() {
        // Act
        String toString = song.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("Song"));
        assertTrue(toString.contains("Test Song"));
    }

    @Test
    void testSongWithNullFields() {
        // Arrange
        Song songWithNulls = Song.builder()
                .id(1L)
                .title("Test Song")
                .artist(null)
                .album(null)
                .year(null)
                .user(testUser)
                .createdAt(null)
                .build();

        // Act & Assert
        assertEquals("Test Song", songWithNulls.getTitle());
        assertNull(songWithNulls.getArtist());
        assertNull(songWithNulls.getAlbum());
        assertNull(songWithNulls.getYear());
        assertNotNull(songWithNulls.getUser());
        assertNull(songWithNulls.getCreatedAt());
    }

    @Test
    void testSongWithMinimalData() {
        // Arrange
        Song minimalSong = Song.builder()
                .title("Minimal Song")
                .user(testUser)
                .build();

        // Act & Assert
        assertEquals(0L, minimalSong.getId());
        assertEquals("Minimal Song", minimalSong.getTitle());
        assertNull(minimalSong.getArtist());
        assertNull(minimalSong.getAlbum());
        assertNull(minimalSong.getYear());
        assertEquals(testUser, minimalSong.getUser());
        assertNull(minimalSong.getCreatedAt());
    }

    @Test
    void testSongSetters() {
        // Arrange
        Song newSong = new Song();
        LocalDateTime testTime = LocalDateTime.now();

        // Act
        newSong.setId(5L);
        newSong.setTitle("Setter Song");
        newSong.setArtist("Setter Artist");
        newSong.setAlbum("Setter Album");
        newSong.setYear(2025);
        newSong.setUser(testUser);
        newSong.setCreatedAt(testTime);

        // Assert
        assertEquals(5L, newSong.getId());
        assertEquals("Setter Song", newSong.getTitle());
        assertEquals("Setter Artist", newSong.getArtist());
        assertEquals("Setter Album", newSong.getAlbum());
        assertEquals(2025, newSong.getYear());
        assertEquals(testUser, newSong.getUser());
        assertEquals(testTime, newSong.getCreatedAt());
    }

    @Test
    void testSongWithDifferentYears() {
        // Arrange
        Song oldSong = Song.builder()
                .title("Old Song")
                .year(1990)
                .user(testUser)
                .build();

        Song newSong = Song.builder()
                .title("New Song")
                .year(2024)
                .user(testUser)
                .build();

        // Act & Assert
        assertEquals(1990, oldSong.getYear());
        assertEquals(2024, newSong.getYear());
        assertNotEquals(oldSong.getYear(), newSong.getYear());
    }

    @Test
    void testSongWithDifferentUsers() {
        // Arrange
        User user1 = User.builder()
                .id(1L)
                .username("user1")
                .role(Role.USER)
                .build();

        User user2 = User.builder()
                .id(2L)
                .username("user2")
                .role(Role.ADMIN)
                .build();

        Song song1 = Song.builder()
                .title("Song 1")
                .user(user1)
                .build();

        Song song2 = Song.builder()
                .title("Song 2")
                .user(user2)
                .build();

        // Act & Assert
        assertEquals(user1, song1.getUser());
        assertEquals(user2, song2.getUser());
        assertNotEquals(song1.getUser(), song2.getUser());
    }
}
