package com.misacordes.application.dto.response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SongResponseTest {

    private SongResponse songResponse;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        testDateTime = LocalDateTime.of(2023, 6, 15, 14, 30, 0);
        
        songResponse = SongResponse.builder()
                .id(1L)
                .title("Test Song")
                .artist("Test Artist")
                .album("Test Album")
                .year(2023)
                .createdAt(testDateTime)
                .build();
    }

    @Test
    void testSongResponseBuilder() {
        // Assert
        assertEquals(1L, songResponse.getId());
        assertEquals("Test Song", songResponse.getTitle());
        assertEquals("Test Artist", songResponse.getArtist());
        assertEquals("Test Album", songResponse.getAlbum());
        assertEquals(2023, songResponse.getYear());
        assertEquals(testDateTime, songResponse.getCreatedAt());
    }

    @Test
    void testSongResponseNoArgsConstructor() {
        // Act
        SongResponse emptyResponse = new SongResponse();

        // Assert
        assertNotNull(emptyResponse);
        assertEquals(0L, emptyResponse.getId());
        assertNull(emptyResponse.getTitle());
        assertNull(emptyResponse.getArtist());
        assertNull(emptyResponse.getAlbum());
        assertNull(emptyResponse.getYear());
        assertNull(emptyResponse.getCreatedAt());
    }

    @Test
    void testSongResponseAllArgsConstructor() {
        // Arrange
        LocalDateTime newDateTime = LocalDateTime.of(2024, 1, 1, 12, 0, 0);

        // Act
        SongResponse newResponse = new SongResponse(2L, "New Song", "New Artist", "New Album", 2024, newDateTime);

        // Assert
        assertEquals(2L, newResponse.getId());
        assertEquals("New Song", newResponse.getTitle());
        assertEquals("New Artist", newResponse.getArtist());
        assertEquals("New Album", newResponse.getAlbum());
        assertEquals(2024, newResponse.getYear());
        assertEquals(newDateTime, newResponse.getCreatedAt());
    }

    @Test
    void testSongResponseSetters() {
        // Arrange
        SongResponse response = new SongResponse();
        LocalDateTime setterDateTime = LocalDateTime.of(2025, 3, 10, 16, 45, 30);

        // Act
        response.setId(5L);
        response.setTitle("Setter Song");
        response.setArtist("Setter Artist");
        response.setAlbum("Setter Album");
        response.setYear(2025);
        response.setCreatedAt(setterDateTime);

        // Assert
        assertEquals(5L, response.getId());
        assertEquals("Setter Song", response.getTitle());
        assertEquals("Setter Artist", response.getArtist());
        assertEquals("Setter Album", response.getAlbum());
        assertEquals(2025, response.getYear());
        assertEquals(setterDateTime, response.getCreatedAt());
    }

    @Test
    void testSongResponseEquality() {
        // Arrange
        SongResponse response1 = SongResponse.builder()
                .id(1L)
                .title("Test Song")
                .artist("Test Artist")
                .album("Test Album")
                .year(2023)
                .createdAt(testDateTime)
                .build();

        SongResponse response2 = SongResponse.builder()
                .id(1L)
                .title("Test Song")
                .artist("Test Artist")
                .album("Test Album")
                .year(2023)
                .createdAt(testDateTime)
                .build();

        // Act & Assert
        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
    }

    @Test
    void testSongResponseInequality() {
        // Arrange
        SongResponse response1 = SongResponse.builder()
                .id(1L)
                .title("Song 1")
                .artist("Artist 1")
                .album("Album 1")
                .year(2023)
                .createdAt(testDateTime)
                .build();

        SongResponse response2 = SongResponse.builder()
                .id(2L)
                .title("Song 2")
                .artist("Artist 2")
                .album("Album 2")
                .year(2024)
                .createdAt(LocalDateTime.now())
                .build();

        // Act & Assert
        assertNotEquals(response1, response2);
    }

    @Test
    void testSongResponseToString() {
        // Act
        String toString = songResponse.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("SongResponse"));
        assertTrue(toString.contains("Test Song"));
    }

    @Test
    void testSongResponseWithNullFields() {
        // Arrange
        SongResponse responseWithNulls = SongResponse.builder()
                .id(1L)
                .title(null)
                .artist(null)
                .album(null)
                .year(null)
                .createdAt(null)
                .build();

        // Act & Assert
        assertEquals(1L, responseWithNulls.getId());
        assertNull(responseWithNulls.getTitle());
        assertNull(responseWithNulls.getArtist());
        assertNull(responseWithNulls.getAlbum());
        assertNull(responseWithNulls.getYear());
        assertNull(responseWithNulls.getCreatedAt());
    }

    @Test
    void testSongResponseWithEmptyStrings() {
        // Arrange
        SongResponse responseWithEmpties = SongResponse.builder()
                .id(1L)
                .title("")
                .artist("")
                .album("")
                .year(0)
                .createdAt(testDateTime)
                .build();

        // Act & Assert
        assertEquals(1L, responseWithEmpties.getId());
        assertEquals("", responseWithEmpties.getTitle());
        assertEquals("", responseWithEmpties.getArtist());
        assertEquals("", responseWithEmpties.getAlbum());
        assertEquals(0, responseWithEmpties.getYear());
        assertEquals(testDateTime, responseWithEmpties.getCreatedAt());
    }

    @Test
    void testSongResponseWithMinimalData() {
        // Arrange
        SongResponse minimalResponse = SongResponse.builder()
                .id(1L)
                .title("Minimal Song")
                .createdAt(testDateTime)
                .build();

        // Act & Assert
        assertEquals(1L, minimalResponse.getId());
        assertEquals("Minimal Song", minimalResponse.getTitle());
        assertNull(minimalResponse.getArtist());
        assertNull(minimalResponse.getAlbum());
        assertNull(minimalResponse.getYear());
        assertEquals(testDateTime, minimalResponse.getCreatedAt());
    }

    @Test
    void testSongResponseWithDifferentYears() {
        // Arrange
        SongResponse oldSong = SongResponse.builder()
                .id(1L)
                .title("Old Song")
                .year(1990)
                .createdAt(testDateTime)
                .build();

        SongResponse newSong = SongResponse.builder()
                .id(2L)
                .title("New Song")
                .year(2024)
                .createdAt(testDateTime)
                .build();

        // Act & Assert
        assertEquals(1990, oldSong.getYear());
        assertEquals(2024, newSong.getYear());
        assertNotEquals(oldSong.getYear(), newSong.getYear());
    }

    @Test
    void testSongResponseWithDifferentTimestamps() {
        // Arrange
        LocalDateTime oldTime = LocalDateTime.of(2020, 1, 1, 0, 0, 0);
        LocalDateTime newTime = LocalDateTime.of(2024, 12, 31, 23, 59, 59);

        SongResponse oldSong = SongResponse.builder()
                .id(1L)
                .title("Old Song")
                .createdAt(oldTime)
                .build();

        SongResponse newSong = SongResponse.builder()
                .id(2L)
                .title("New Song")
                .createdAt(newTime)
                .build();

        // Act & Assert
        assertEquals(oldTime, oldSong.getCreatedAt());
        assertEquals(newTime, newSong.getCreatedAt());
        assertTrue(oldSong.getCreatedAt().isBefore(newSong.getCreatedAt()));
    }

    @Test
    void testSongResponseWithLongStrings() {
        // Arrange
        String longTitle = "This is a very long song title that might be used in some cases";
        String longArtist = "This is a very long artist name that might be used in some cases";
        String longAlbum = "This is a very long album name that might be used in some cases";

        SongResponse longResponse = SongResponse.builder()
                .id(1L)
                .title(longTitle)
                .artist(longArtist)
                .album(longAlbum)
                .year(2023)
                .createdAt(testDateTime)
                .build();

        // Act & Assert
        assertEquals(longTitle, longResponse.getTitle());
        assertEquals(longArtist, longResponse.getArtist());
        assertEquals(longAlbum, longResponse.getAlbum());
        assertEquals(2023, longResponse.getYear());
        assertEquals(testDateTime, longResponse.getCreatedAt());
    }
}
