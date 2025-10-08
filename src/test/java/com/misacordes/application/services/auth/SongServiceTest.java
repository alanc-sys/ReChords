package com.misacordes.application.services.auth;

import com.misacordes.application.dto.request.SongRequest;
import com.misacordes.application.dto.response.SongResponse;
import com.misacordes.application.entities.Role;
import com.misacordes.application.entities.Song;
import com.misacordes.application.entities.User;
import com.misacordes.application.repositories.ChordCatalogRepository;
import com.misacordes.application.repositories.SongChordRepository;
import com.misacordes.application.repositories.SongRepository;
import com.misacordes.application.repositories.UserRepository;
import com.misacordes.application.utils.SongStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SongServiceTest {

    @Mock
    private SongRepository songRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private SongChordRepository songChordRepository;
    
    @Mock
    private ChordCatalogRepository chordCatalogRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private SongService songService;

    private User testUser;
    private Song testSong;
    private SongRequest songRequest;

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

        testSong = Song.builder()
                .id(1L)
                .title("Test Song")
                .artist("Test Artist")
                .album("Test Album")
                .year(2023)
                .createdBy(testUser)
                .lyricsData("Test lyrics")
                .status(SongStatus.DRAFT)
                .isPublic(false)
                .createdAt(LocalDateTime.now())
                .build();

        songRequest = SongRequest.builder()
                .title("New Song")
                .artist("New Artist")
                .album("New Album")
                .year(2024)
                .lyricsData("New lyrics")
                .build();

        // Configurar el contexto de seguridad
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        
        // Mock para obtener posiciones de acordes (lista vacía por defecto)
        lenient().when(songChordRepository.findBySongIdOrderByLineNumberAscPositionStartAsc(anyLong()))
                .thenReturn(java.util.Collections.emptyList());
    }

    @Test
    void testCreateSong_Success() {
        // Arrange
        Song savedSong = Song.builder()
                .id(2L)
                .title(songRequest.getTitle())
                .artist(songRequest.getArtist())
                .album(songRequest.getAlbum())
                .year(songRequest.getYear())
                .createdBy(testUser)
                .lyricsData(songRequest.getLyricsData())
                .status(SongStatus.DRAFT)
                .isPublic(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(songRepository.save(any(Song.class))).thenReturn(savedSong);

        // Act
        SongResponse response = songService.createSong(songRequest);

        // Assert
        assertNotNull(response);
        assertEquals(savedSong.getId(), response.getId());
        assertEquals(songRequest.getTitle(), response.getTitle());
        assertEquals(songRequest.getArtist(), response.getArtist());
        assertEquals(songRequest.getAlbum(), response.getAlbum());
        assertEquals(songRequest.getYear(), response.getYear());
        assertEquals(songRequest.getLyricsData(), response.getLyrics());
        assertEquals(SongStatus.DRAFT, response.getStatus());
        assertEquals(false, response.getIsPublic());

        verify(songRepository).save(argThat(song ->
                song.getTitle().equals(songRequest.getTitle()) &&
                song.getArtist().equals(songRequest.getArtist()) &&
                song.getAlbum().equals(songRequest.getAlbum()) &&
                song.getYear().equals(songRequest.getYear()) &&
                song.getLyricsData().equals(songRequest.getLyricsData()) &&
                song.getCreatedBy().equals(testUser) &&
                song.getStatus().equals(SongStatus.DRAFT) &&
                song.getIsPublic().equals(false)
        ));
    }

    @Test
    void testCreateSong_WithNullFields() {
        // Arrange
        SongRequest requestWithNulls = SongRequest.builder()
                .title("Song Title")
                .artist(null)
                .album(null)
                .year(null)
                .lyricsData(null)
                .build();

        Song savedSong = Song.builder()
                .id(2L)
                .title(requestWithNulls.getTitle())
                .artist(null)
                .album(null)
                .year(null)
                .createdBy(testUser)
                .lyricsData(null)
                .status(SongStatus.DRAFT)
                .isPublic(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(songRepository.save(any(Song.class))).thenReturn(savedSong);

        // Act
        SongResponse response = songService.createSong(requestWithNulls);

        // Assert
        assertNotNull(response);
        assertEquals(requestWithNulls.getTitle(), response.getTitle());
        assertNull(response.getArtist());
        assertNull(response.getAlbum());
        assertNull(response.getYear());
        assertNull(response.getLyrics());

        verify(songRepository).save(argThat(song ->
                song.getTitle().equals(requestWithNulls.getTitle()) &&
                song.getArtist() == null &&
                song.getAlbum() == null &&
                song.getYear() == null &&
                song.getLyricsData() == null &&
                song.getCreatedBy().equals(testUser) &&
                song.getStatus().equals(SongStatus.DRAFT) &&
                song.getIsPublic().equals(false)
        ));
    }

    @Test
    void testGetMySongs_Success() {
        // Arrange
        Song song1 = Song.builder()
                .id(1L)
                .title("Song 1")
                .artist("Artist 1")
                .album("Album 1")
                .year(2023)
                .createdBy(testUser)
                .lyricsData("Lyrics 1")
                .status(SongStatus.DRAFT)
                .isPublic(false)
                .createdAt(LocalDateTime.now())
                .build();

        Song song2 = Song.builder()
                .id(2L)
                .title("Song 2")
                .artist("Artist 2")
                .album("Album 2")
                .year(2024)
                .createdBy(testUser)
                .lyricsData("Lyrics 2")
                .status(SongStatus.APPROVED)
                .isPublic(true)
                .createdAt(LocalDateTime.now())
                .build();

        List<Song> songs = Arrays.asList(song1, song2);
        when(songRepository.findByCreatedById(testUser.getId())).thenReturn(songs);

        // Act
        List<SongResponse> responses = songService.getMySongs();

        // Assert
        assertNotNull(responses);
        assertEquals(2, responses.size());
        
        SongResponse response1 = responses.get(0);
        assertEquals(song1.getId(), response1.getId());
        assertEquals(song1.getTitle(), response1.getTitle());
        assertEquals(song1.getArtist(), response1.getArtist());
        assertEquals(song1.getAlbum(), response1.getAlbum());
        assertEquals(song1.getYear(), response1.getYear());
        assertEquals(song1.getLyricsData(), response1.getLyrics());
        assertEquals(song1.getStatus(), response1.getStatus());
        assertEquals(song1.getIsPublic(), response1.getIsPublic());
        assertEquals(song1.getCreatedAt(), response1.getCreatedAt());

        SongResponse response2 = responses.get(1);
        assertEquals(song2.getId(), response2.getId());
        assertEquals(song2.getTitle(), response2.getTitle());
        assertEquals(song2.getArtist(), response2.getArtist());
        assertEquals(song2.getAlbum(), response2.getAlbum());
        assertEquals(song2.getYear(), response2.getYear());
        assertEquals(song2.getLyricsData(), response2.getLyrics());
        assertEquals(song2.getStatus(), response2.getStatus());
        assertEquals(song2.getIsPublic(), response2.getIsPublic());
        assertEquals(song2.getCreatedAt(), response2.getCreatedAt());

        verify(songRepository).findByCreatedById(testUser.getId());
    }

    @Test
    void testGetMySongs_EmptyList() {
        // Arrange
        when(songRepository.findByCreatedById(testUser.getId())).thenReturn(Arrays.asList());

        // Act
        List<SongResponse> responses = songService.getMySongs();

        // Assert
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        verify(songRepository).findByCreatedById(testUser.getId());
    }

    @Test
    void testGetSongById_Success() {
        // Arrange
        when(songRepository.findById(1L)).thenReturn(Optional.of(testSong));

        // Act
        SongResponse response = songService.getSongById(1L);

        // Assert
        assertNotNull(response);
        assertEquals(testSong.getId(), response.getId());
        assertEquals(testSong.getTitle(), response.getTitle());
        assertEquals(testSong.getArtist(), response.getArtist());
        assertEquals(testSong.getAlbum(), response.getAlbum());
        assertEquals(testSong.getYear(), response.getYear());
        assertEquals(testSong.getLyricsData(), response.getLyrics());
        assertEquals(testSong.getStatus(), response.getStatus());
        assertEquals(testSong.getIsPublic(), response.getIsPublic());
        assertEquals(testSong.getCreatedAt(), response.getCreatedAt());

        verify(songRepository).findById(1L);
    }

    @Test
    void testGetSongById_SongNotFound() {
        // Arrange
        when(songRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> songService.getSongById(999L)
        );
        
        assertEquals("Song not found", exception.getMessage());
        verify(songRepository).findById(999L);
    }

    // Los métodos getCurrentUser() y mapToResponse() son privados,
    // por lo que se testean indirectamente a través de los métodos públicos

    @Test
    void testCreateSong_WithMinimalData() {
        // Arrange
        SongRequest minimalRequest = SongRequest.builder()
                .title("Minimal Song")
                .build();

        Song savedSong = Song.builder()
                .id(2L)
                .title(minimalRequest.getTitle())
                .artist(null)
                .album(null)
                .year(null)
                .createdBy(testUser)
                .lyricsData(null)
                .status(SongStatus.DRAFT)
                .isPublic(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(songRepository.save(any(Song.class))).thenReturn(savedSong);

        // Act
        SongResponse response = songService.createSong(minimalRequest);

        // Assert
        assertNotNull(response);
        assertEquals(minimalRequest.getTitle(), response.getTitle());
        assertNull(response.getArtist());
        assertNull(response.getAlbum());
        assertNull(response.getYear());
        assertNull(response.getLyrics());

        verify(songRepository).save(argThat(song ->
                song.getTitle().equals(minimalRequest.getTitle()) &&
                song.getArtist() == null &&
                song.getAlbum() == null &&
                song.getYear() == null &&
                song.getLyricsData() == null &&
                song.getCreatedBy().equals(testUser) &&
                song.getStatus().equals(SongStatus.DRAFT) &&
                song.getIsPublic().equals(false)
        ));
    }
}
