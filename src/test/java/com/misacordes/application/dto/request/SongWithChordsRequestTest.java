package com.misacordes.application.dto.request;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SongWithChordsRequestTest {

    @Test
    void testSongWithChordsRequest_AllArgsConstructor() {
        // Arrange
        List<LineWithChords> lyrics = Arrays.asList(
                new LineWithChords(0, "Test line", Arrays.asList())
        );

        // Act
        SongWithChordsRequest request = new SongWithChordsRequest(
                "Test Song",
                "Test Artist",
                "Test Album",
                2024,
                "C",
                120,
                null,  // youtubeUrl
                null,  // spotifyUrl
                null,  // coverImageUrl
                null,  // coverColor
                lyrics,
                1L,
                null  // proposedChords (opcional)
        );

        // Assert
        assertEquals("Test Song", request.getTitle());
        assertEquals("Test Artist", request.getArtist());
        assertEquals("Test Album", request.getAlbum());
        assertEquals(2024, request.getYear());
        assertEquals("C", request.getKey());
        assertEquals(120, request.getTempo());
        assertEquals(1, request.getLyrics().size());
        assertEquals(1L, request.getCreatedBy());
        assertNull(request.getProposedChords());
    }

    @Test
    void testSongWithChordsRequest_NoArgsConstructor() {
        // Act
        SongWithChordsRequest request = new SongWithChordsRequest();

        // Assert
        assertNull(request.getTitle());
        assertNull(request.getArtist());
        assertNull(request.getAlbum());
        assertNull(request.getYear());
        assertNull(request.getKey());
        assertNull(request.getTempo());
        assertNull(request.getLyrics());
        assertNull(request.getCreatedBy());
    }

    @Test
    void testSongWithChordsRequest_Setters() {
        // Arrange
        SongWithChordsRequest request = new SongWithChordsRequest();
        List<LineWithChords> lyrics = Arrays.asList(
                new LineWithChords(0, "Test line", Arrays.asList())
        );

        // Act
        request.setTitle("Test Song");
        request.setArtist("Test Artist");
        request.setAlbum("Test Album");
        request.setYear(2024);
        request.setKey("C");
        request.setTempo(120);
        request.setLyrics(lyrics);
        request.setCreatedBy(1L);

        // Assert
        assertEquals("Test Song", request.getTitle());
        assertEquals("Test Artist", request.getArtist());
        assertEquals("Test Album", request.getAlbum());
        assertEquals(2024, request.getYear());
        assertEquals("C", request.getKey());
        assertEquals(120, request.getTempo());
        assertEquals(1, request.getLyrics().size());
        assertEquals(1L, request.getCreatedBy());
    }
}
