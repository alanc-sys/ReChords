package com.misacordes.application.dto.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SongRequestTest {

    private SongRequest songRequest;

    @BeforeEach
    void setUp() {
        songRequest = SongRequest.builder()
                .title("Test Song")
                .artist("Test Artist")
                .album("Test Album")
                .year(2023)
                .build();
    }

    @Test
    void testSongRequestBuilder() {
        // Assert
        assertEquals("Test Song", songRequest.getTitle());
        assertEquals("Test Artist", songRequest.getArtist());
        assertEquals("Test Album", songRequest.getAlbum());
        assertEquals(2023, songRequest.getYear());
    }

    @Test
    void testSongRequestNoArgsConstructor() {
        // Act
        SongRequest emptyRequest = new SongRequest();

        // Assert
        assertNotNull(emptyRequest);
        assertNull(emptyRequest.getTitle());
        assertNull(emptyRequest.getArtist());
        assertNull(emptyRequest.getAlbum());
        assertNull(emptyRequest.getYear());
    }

    @Test
    void testSongRequestAllArgsConstructor() {
        // Act
        SongRequest newRequest = new SongRequest("New Song", "New Artist", "New Album", 2024);

        // Assert
        assertEquals("New Song", newRequest.getTitle());
        assertEquals("New Artist", newRequest.getArtist());
        assertEquals("New Album", newRequest.getAlbum());
        assertEquals(2024, newRequest.getYear());
    }

    @Test
    void testSongRequestSetters() {
        // Arrange
        SongRequest request = new SongRequest();

        // Act
        request.setTitle("Setter Song");
        request.setArtist("Setter Artist");
        request.setAlbum("Setter Album");
        request.setYear(2025);

        // Assert
        assertEquals("Setter Song", request.getTitle());
        assertEquals("Setter Artist", request.getArtist());
        assertEquals("Setter Album", request.getAlbum());
        assertEquals(2025, request.getYear());
    }

    @Test
    void testSongRequestEquality() {
        // Arrange
        SongRequest request1 = SongRequest.builder()
                .title("Test Song")
                .artist("Test Artist")
                .album("Test Album")
                .year(2023)
                .build();

        SongRequest request2 = SongRequest.builder()
                .title("Test Song")
                .artist("Test Artist")
                .album("Test Album")
                .year(2023)
                .build();

        // Act & Assert
        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
    }

    @Test
    void testSongRequestInequality() {
        // Arrange
        SongRequest request1 = SongRequest.builder()
                .title("Song 1")
                .artist("Artist 1")
                .album("Album 1")
                .year(2023)
                .build();

        SongRequest request2 = SongRequest.builder()
                .title("Song 2")
                .artist("Artist 2")
                .album("Album 2")
                .year(2024)
                .build();

        // Act & Assert
        assertNotEquals(request1, request2);
    }

    @Test
    void testSongRequestToString() {
        // Act
        String toString = songRequest.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("SongRequest"));
        assertTrue(toString.contains("Test Song"));
    }

    @Test
    void testSongRequestWithNullFields() {
        // Arrange
        SongRequest requestWithNulls = SongRequest.builder()
                .title(null)
                .artist(null)
                .album(null)
                .year(null)
                .build();

        // Act & Assert
        assertNull(requestWithNulls.getTitle());
        assertNull(requestWithNulls.getArtist());
        assertNull(requestWithNulls.getAlbum());
        assertNull(requestWithNulls.getYear());
    }

    @Test
    void testSongRequestWithEmptyStrings() {
        // Arrange
        SongRequest requestWithEmpties = SongRequest.builder()
                .title("")
                .artist("")
                .album("")
                .year(0)
                .build();

        // Act & Assert
        assertEquals("", requestWithEmpties.getTitle());
        assertEquals("", requestWithEmpties.getArtist());
        assertEquals("", requestWithEmpties.getAlbum());
        assertEquals(0, requestWithEmpties.getYear());
    }

    @Test
    void testSongRequestWithMinimalData() {
        // Arrange
        SongRequest minimalRequest = SongRequest.builder()
                .title("Minimal Song")
                .build();

        // Act & Assert
        assertEquals("Minimal Song", minimalRequest.getTitle());
        assertNull(minimalRequest.getArtist());
        assertNull(minimalRequest.getAlbum());
        assertNull(minimalRequest.getYear());
    }

    @Test
    void testSongRequestWithDifferentYears() {
        // Arrange
        SongRequest oldSong = SongRequest.builder()
                .title("Old Song")
                .year(1990)
                .build();

        SongRequest newSong = SongRequest.builder()
                .title("New Song")
                .year(2024)
                .build();

        // Act & Assert
        assertEquals(1990, oldSong.getYear());
        assertEquals(2024, newSong.getYear());
        assertNotEquals(oldSong.getYear(), newSong.getYear());
    }

    @Test
    void testSongRequestWithLongStrings() {
        // Arrange
        String longTitle = "This is a very long song title that might be used in some cases";
        String longArtist = "This is a very long artist name that might be used in some cases";
        String longAlbum = "This is a very long album name that might be used in some cases";

        SongRequest longRequest = SongRequest.builder()
                .title(longTitle)
                .artist(longArtist)
                .album(longAlbum)
                .year(2023)
                .build();

        // Act & Assert
        assertEquals(longTitle, longRequest.getTitle());
        assertEquals(longArtist, longRequest.getArtist());
        assertEquals(longAlbum, longRequest.getAlbum());
        assertEquals(2023, longRequest.getYear());
    }
}
