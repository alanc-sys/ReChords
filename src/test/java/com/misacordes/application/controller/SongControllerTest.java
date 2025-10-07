package com.misacordes.application.controller;

import com.misacordes.application.dto.request.SongRequest;
import com.misacordes.application.dto.response.SongResponse;
import com.misacordes.application.services.auth.SongService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SongControllerTest {

    @Mock
    private SongService songService;

    @InjectMocks
    private songController songController;

    private SongRequest songRequest;
    private SongResponse songResponse;
    private List<SongResponse> songResponses;

    @BeforeEach
    void setUp() {
        songRequest = SongRequest.builder()
                .title("Test Song")
                .artist("Test Artist")
                .album("Test Album")
                .year(2023)
                .build();

        songResponse = SongResponse.builder()
                .id(1L)
                .title("Test Song")
                .artist("Test Artist")
                .album("Test Album")
                .year(2023)
                .createdAt(LocalDateTime.now())
                .build();

        songResponses = Arrays.asList(
                SongResponse.builder()
                        .id(1L)
                        .title("Song 1")
                        .artist("Artist 1")
                        .album("Album 1")
                        .year(2023)
                        .createdAt(LocalDateTime.now())
                        .build(),
                SongResponse.builder()
                        .id(2L)
                        .title("Song 2")
                        .artist("Artist 2")
                        .album("Album 2")
                        .year(2024)
                        .createdAt(LocalDateTime.now())
                        .build()
        );
    }

    @Test
    void testCreatedSong_Success() {
        // Arrange
        when(songService.createSong(songRequest)).thenReturn(songResponse);

        // Act
        ResponseEntity<SongResponse> response = songController.createdSong(songRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(songResponse.getId(), response.getBody().getId());
        assertEquals(songResponse.getTitle(), response.getBody().getTitle());
        assertEquals(songResponse.getArtist(), response.getBody().getArtist());
        assertEquals(songResponse.getAlbum(), response.getBody().getAlbum());
        assertEquals(songResponse.getYear(), response.getBody().getYear());
        assertEquals(songResponse.getCreatedAt(), response.getBody().getCreatedAt());

        verify(songService).createSong(songRequest);
    }

    @Test
    void testCreatedSong_WithNullRequest() {
        // Act & Assert - El controlador no valida null, simplemente pasa el null al servicio
        assertDoesNotThrow(() -> songController.createdSong(null));
    }

    @Test
    void testCreatedSong_ServiceThrowsException() {
        // Arrange
        RuntimeException serviceException = new RuntimeException("Service error");
        when(songService.createSong(songRequest)).thenThrow(serviceException);

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> songController.createdSong(songRequest)
        );
        
        assertEquals("Service error", exception.getMessage());
        verify(songService).createSong(songRequest);
    }

    @Test
    void testGetAllSongs_Success() {
        // Arrange
        when(songService.getAllSongs()).thenReturn(songResponses);

        // Act
        ResponseEntity<List<SongResponse>> response = songController.getAllSongs();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        
        SongResponse response1 = response.getBody().get(0);
        assertEquals(songResponses.get(0).getId(), response1.getId());
        assertEquals(songResponses.get(0).getTitle(), response1.getTitle());
        assertEquals(songResponses.get(0).getArtist(), response1.getArtist());
        assertEquals(songResponses.get(0).getAlbum(), response1.getAlbum());
        assertEquals(songResponses.get(0).getYear(), response1.getYear());

        SongResponse response2 = response.getBody().get(1);
        assertEquals(songResponses.get(1).getId(), response2.getId());
        assertEquals(songResponses.get(1).getTitle(), response2.getTitle());
        assertEquals(songResponses.get(1).getArtist(), response2.getArtist());
        assertEquals(songResponses.get(1).getAlbum(), response2.getAlbum());
        assertEquals(songResponses.get(1).getYear(), response2.getYear());

        verify(songService).getAllSongs();
    }

    @Test
    void testGetAllSongs_EmptyList() {
        // Arrange
        when(songService.getAllSongs()).thenReturn(Arrays.asList());

        // Act
        ResponseEntity<List<SongResponse>> response = songController.getAllSongs();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());

        verify(songService).getAllSongs();
    }

    @Test
    void testGetAllSongs_ServiceThrowsException() {
        // Arrange
        RuntimeException serviceException = new RuntimeException("Service error");
        when(songService.getAllSongs()).thenThrow(serviceException);

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> songController.getAllSongs()
        );
        
        assertEquals("Service error", exception.getMessage());
        verify(songService).getAllSongs();
    }

    @Test
    void testSongDetail_Success() {
        // Arrange
        when(songService.songDetail(1L)).thenReturn(songResponse);

        // Act
        ResponseEntity<SongResponse> response = songController.songDetail(1L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(songResponse.getId(), response.getBody().getId());
        assertEquals(songResponse.getTitle(), response.getBody().getTitle());
        assertEquals(songResponse.getArtist(), response.getBody().getArtist());
        assertEquals(songResponse.getAlbum(), response.getBody().getAlbum());
        assertEquals(songResponse.getYear(), response.getBody().getYear());
        assertEquals(songResponse.getCreatedAt(), response.getBody().getCreatedAt());

        verify(songService).songDetail(1L);
    }

    @Test
    void testSongDetail_ServiceThrowsException() {
        // Arrange
        RuntimeException serviceException = new RuntimeException("Song not found");
        when(songService.songDetail(999L)).thenThrow(serviceException);

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> songController.songDetail(999L)
        );
        
        assertEquals("Song not found", exception.getMessage());
        verify(songService).songDetail(999L);
    }

    @Test
    void testDeleteSong_Success() {
        // Act
        ResponseEntity<SongResponse> response = songController.deleteSong(1L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());

        verify(songService).deleteSong(1L);
    }

    @Test
    void testDeleteSong_ServiceThrowsException() {
        // Arrange
        RuntimeException serviceException = new RuntimeException("Song not found");
        doThrow(serviceException).when(songService).deleteSong(999L);

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> songController.deleteSong(999L)
        );
        
        assertEquals("Song not found", exception.getMessage());
        verify(songService).deleteSong(999L);
    }

    @Test
    void testCreatedSong_WithMinimalData() {
        // Arrange
        SongRequest minimalRequest = SongRequest.builder()
                .title("Minimal Song")
                .build();

        SongResponse minimalResponse = SongResponse.builder()
                .id(1L)
                .title("Minimal Song")
                .artist(null)
                .album(null)
                .year(null)
                .createdAt(LocalDateTime.now())
                .build();

        when(songService.createSong(minimalRequest)).thenReturn(minimalResponse);

        // Act
        ResponseEntity<SongResponse> response = songController.createdSong(minimalRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(minimalResponse.getTitle(), response.getBody().getTitle());
        assertNull(response.getBody().getArtist());
        assertNull(response.getBody().getAlbum());
        assertNull(response.getBody().getYear());

        verify(songService).createSong(minimalRequest);
    }

    @Test
    void testCreatedSong_WithNullFields() {
        // Arrange
        SongRequest requestWithNulls = SongRequest.builder()
                .title("Song Title")
                .artist(null)
                .album(null)
                .year(null)
                .build();

        SongResponse responseWithNulls = SongResponse.builder()
                .id(1L)
                .title("Song Title")
                .artist(null)
                .album(null)
                .year(null)
                .createdAt(LocalDateTime.now())
                .build();

        when(songService.createSong(requestWithNulls)).thenReturn(responseWithNulls);

        // Act
        ResponseEntity<SongResponse> response = songController.createdSong(requestWithNulls);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(requestWithNulls.getTitle(), response.getBody().getTitle());
        assertNull(response.getBody().getArtist());
        assertNull(response.getBody().getAlbum());
        assertNull(response.getBody().getYear());

        verify(songService).createSong(requestWithNulls);
    }

    @Test
    void testSongDetail_WithDifferentIds() {
        // Arrange
        SongResponse response1 = SongResponse.builder()
                .id(1L)
                .title("Song 1")
                .build();

        SongResponse response2 = SongResponse.builder()
                .id(2L)
                .title("Song 2")
                .build();

        when(songService.songDetail(1L)).thenReturn(response1);
        when(songService.songDetail(2L)).thenReturn(response2);

        // Act
        ResponseEntity<SongResponse> result1 = songController.songDetail(1L);
        ResponseEntity<SongResponse> result2 = songController.songDetail(2L);

        // Assert
        assertNotNull(result1);
        assertEquals(HttpStatus.OK, result1.getStatusCode());
        assertEquals(1L, result1.getBody().getId());
        assertEquals("Song 1", result1.getBody().getTitle());

        assertNotNull(result2);
        assertEquals(HttpStatus.OK, result2.getStatusCode());
        assertEquals(2L, result2.getBody().getId());
        assertEquals("Song 2", result2.getBody().getTitle());

        verify(songService).songDetail(1L);
        verify(songService).songDetail(2L);
    }
}
