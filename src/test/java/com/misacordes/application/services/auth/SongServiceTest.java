package com.misacordes.application.services.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.misacordes.application.dto.request.SongWithChordsRequest;
import com.misacordes.application.dto.response.SongWithChordsResponse;
import com.misacordes.application.entities.Role;
import com.misacordes.application.entities.Song;
import com.misacordes.application.entities.User;
import com.misacordes.application.repositories.SongRepository;
import com.misacordes.application.repositories.UserRepository;
import com.misacordes.application.services.SongAnalyticsService;
import com.misacordes.application.services.SongAnalyticsAsyncService;
import com.misacordes.application.services.SongService;
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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.misacordes.application.dto.response.PageResponse;

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
    private SongAnalyticsService songAnalyticsService;
    
    @Mock
    private SongAnalyticsAsyncService songAnalyticsAsyncService;
    
    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private SongService songService;

    private User testUser;
    private Song testSong;
    private SongWithChordsRequest testRequest;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .firstname("Test")
                .lastname("User")
                .role(Role.USER)
                .build();

        // Setup test song
        testSong = Song.builder()
                .id(1L)
                .title("Test Song")
                .artist("Test Artist")
                .album("Test Album")
                .year(2024)
                .createdBy(testUser)
                .chordsMap("{\"title\":\"Test Song\",\"lyrics\":[]}")
                .status(SongStatus.DRAFT)
                .isPublic(false)
                .createdAt(LocalDateTime.now())
                .build();

        // Setup test request
        testRequest = new SongWithChordsRequest();
        testRequest.setTitle("Test Song");
        testRequest.setArtist("Test Artist");
        testRequest.setAlbum("Test Album");
        testRequest.setYear(2024);
        testRequest.setKey("C");
        testRequest.setTempo(120);

        // Setup security context
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getPrincipal()).thenReturn(testUser);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void createSongWithChords_ShouldCreateSongSuccessfully() throws Exception {
        // Arrange
        String jsonString = "{\"title\":\"Test Song\",\"lyrics\":[]}";
        when(objectMapper.writeValueAsString(any())).thenReturn(jsonString);
        when(songAnalyticsService.validateChordsMap(jsonString)).thenReturn(true);
        when(songRepository.save(any(Song.class))).thenReturn(testSong);

        // Act
        SongWithChordsResponse response = songService.createSongWithChords(testRequest);

        // Assert
        assertNotNull(response);
        assertEquals("Test Song", response.getTitle());
        assertEquals("Test Artist", response.getArtist());
        assertEquals(SongStatus.DRAFT, response.getStatus());
        
        verify(songRepository).save(any(Song.class));
        verify(songAnalyticsAsyncService).processSongAnalyticsAsync(1L);
    }

    @Test
    void createSongWithChords_ShouldThrowException_WhenInvalidChordsMap() throws Exception {
        // Arrange
        String jsonString = "{\"title\":\"Test Song\",\"lyrics\":[]}";
        when(objectMapper.writeValueAsString(any())).thenReturn(jsonString);
        when(songAnalyticsService.validateChordsMap(jsonString)).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            songService.createSongWithChords(testRequest);
        });
        
        verify(songRepository, never()).save(any(Song.class));
    }

    @Test
    void getSongWithChordsById_ShouldReturnSong_WhenUserCanView() {
        // Arrange
        when(songRepository.findById(1L)).thenReturn(Optional.of(testSong));

        // Act
        SongWithChordsResponse response = songService.getSongWithChordsById(1L);

        // Assert
        assertNotNull(response);
        assertEquals("Test Song", response.getTitle());
        assertEquals("Test Artist", response.getArtist());
    }

    @Test
    void getSongWithChordsById_ShouldThrowException_WhenSongNotFound() {
        // Arrange
        when(songRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            songService.getSongWithChordsById(1L);
        });
    }

    @Test
    void updateSongWithChords_ShouldUpdateSongSuccessfully() throws Exception {
        // Arrange
        String jsonString = "{\"title\":\"Updated Song\",\"lyrics\":[]}";
        when(songRepository.findByIdAndCreatedById(1L, 1L)).thenReturn(Optional.of(testSong));
        when(objectMapper.writeValueAsString(any())).thenReturn(jsonString);
        when(songAnalyticsService.validateChordsMap(jsonString)).thenReturn(true);
        when(songRepository.save(any(Song.class))).thenReturn(testSong);

        // Act
        SongWithChordsResponse response = songService.updateSongWithChords(1L, testRequest);

        // Assert
        assertNotNull(response);
        verify(songRepository).save(any(Song.class));
        verify(songAnalyticsAsyncService).processSongAnalyticsAsync(1L);
    }

    @Test
    void updateSongWithChords_ShouldThrowException_WhenSongNotOwned() {
        // Arrange
        when(songRepository.findByIdAndCreatedById(1L, 1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            songService.updateSongWithChords(1L, testRequest);
        });
    }

    @Test
    void getMySongsWithChordsPaginated_ShouldReturnUserSongsPaginated() {
        // Arrange
        List<Song> songs = Arrays.asList(testSong);
        Page<Song> songPage = new PageImpl<>(songs);
        Pageable pageable = PageRequest.of(0, 10);
        when(songRepository.findByCreatedById(1L, pageable)).thenReturn(songPage);

        // Act
        PageResponse<SongWithChordsResponse> response = songService.getMySongsWithChordsPaginated(pageable);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("Test Song", response.getContent().get(0).getTitle());
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        verify(songRepository).findByCreatedById(1L, pageable);
    }

    @Test
    void getPublicSongsWithChordsPaginated_ShouldReturnPublicSongsPaginated() {
        // Arrange
        testSong.setStatus(SongStatus.APPROVED);
        testSong.setIsPublic(true);
        List<Song> songs = Arrays.asList(testSong);
        Page<Song> songPage = new PageImpl<>(songs);
        Pageable pageable = PageRequest.of(0, 10);
        when(songRepository.findByIsPublicTrueAndStatus(SongStatus.APPROVED, pageable)).thenReturn(songPage);

        // Act
        PageResponse<SongWithChordsResponse> response = songService.getPublicSongsWithChordsPaginated(pageable);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("Test Song", response.getContent().get(0).getTitle());
        assertEquals(1, response.getTotalElements());
        verify(songRepository).findByIsPublicTrueAndStatus(SongStatus.APPROVED, pageable);
    }

    @Test
    void searchPublicSongsWithChordsPaginated_ShouldReturnMatchingSongsPaginated() {
        // Arrange
        testSong.setStatus(SongStatus.APPROVED);
        testSong.setIsPublic(true);
        List<Song> songs = Arrays.asList(testSong);
        Page<Song> songPage = new PageImpl<>(songs);
        Pageable pageable = PageRequest.of(0, 10);
        when(songRepository.findByIsPublicTrueAndStatusAndTitleContainingIgnoreCaseOrArtistContainingIgnoreCase(
                eq(SongStatus.APPROVED), eq("test"), eq("test"), eq(pageable))).thenReturn(songPage);

        // Act
        PageResponse<SongWithChordsResponse> response = songService.searchPublicSongsWithChordsPaginated("test", pageable);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("Test Song", response.getContent().get(0).getTitle());
        assertEquals(1, response.getTotalElements());
        verify(songRepository).findByIsPublicTrueAndStatusAndTitleContainingIgnoreCaseOrArtistContainingIgnoreCase(
                SongStatus.APPROVED, "test", "test", pageable);
    }

    @Test
    void submitForApprovalWithChords_ShouldChangeStatusToPending() {
        // Arrange
        when(songRepository.findByIdAndCreatedById(1L, 1L)).thenReturn(Optional.of(testSong));
        when(songRepository.save(any(Song.class))).thenReturn(testSong);

        // Act
        SongWithChordsResponse response = songService.submitForApprovalWithChords(1L);

        // Assert
        assertNotNull(response);
        assertEquals(SongStatus.PENDING, response.getStatus());
        verify(songRepository).save(any(Song.class));
    }

    @Test
    void deleteSong_ShouldDeleteSong_WhenUserOwnsIt() {
        // Arrange
        when(songRepository.findByIdAndCreatedById(1L, 1L)).thenReturn(Optional.of(testSong));

        // Act
        songService.deleteSong(1L);

        // Assert
        verify(songRepository).delete(testSong);
    }

    @Test
    void deleteSong_ShouldThrowException_WhenSongNotOwned() {
        // Arrange
        when(songRepository.findByIdAndCreatedById(1L, 1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            songService.deleteSong(1L);
        });
        
        verify(songRepository, never()).delete(any(Song.class));
    }

    @Test
    void getSongAnalytics_ShouldReturnAnalytics() {
        // Arrange
        when(songRepository.findById(1L)).thenReturn(Optional.of(testSong));
        var mockAnalytics = com.misacordes.application.dto.response.SongAnalyticsResponse.builder()
                .songId(1L)
                .title("Test Song")
                .totalChords(5)
                .build();
        when(songAnalyticsService.analyzeSongChords(testSong)).thenReturn(mockAnalytics);

        // Act
        var response = songService.getSongAnalytics(1L);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getSongId());
        verify(songAnalyticsService).analyzeSongChords(testSong);
    }

    @Test
    void getPendingSongsPaginated_ShouldReturnPendingSongsPaginated() {
        // Arrange
        testSong.setStatus(SongStatus.PENDING);
        List<Song> songs = Arrays.asList(testSong);
        Page<Song> songPage = new PageImpl<>(songs);
        Pageable pageable = PageRequest.of(0, 10);
        when(songRepository.findByStatus(SongStatus.PENDING, pageable)).thenReturn(songPage);
        
        // Mock admin user
        testUser.setRole(Role.ADMIN);
        when(authentication.getPrincipal()).thenReturn(testUser);

        // Act
        PageResponse<SongWithChordsResponse> response = songService.getPendingSongsPaginated(pageable);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("Test Song", response.getContent().get(0).getTitle());
        assertEquals(1, response.getTotalElements());
        verify(songRepository).findByStatus(SongStatus.PENDING, pageable);
    }

    @Test
    void getAllSongsAdminPaginated_ShouldReturnAllSongsPaginated() {
        // Arrange
        List<Song> songs = Arrays.asList(testSong);
        Page<Song> songPage = new PageImpl<>(songs);
        Pageable pageable = PageRequest.of(0, 10);
        when(songRepository.findAll(pageable)).thenReturn(songPage);
        
        // Mock admin user
        testUser.setRole(Role.ADMIN);
        when(authentication.getPrincipal()).thenReturn(testUser);

        // Act
        PageResponse<SongWithChordsResponse> response = songService.getAllSongsAdminPaginated(pageable);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("Test Song", response.getContent().get(0).getTitle());
        assertEquals(1, response.getTotalElements());
        verify(songRepository).findAll(pageable);
    }

    @Test
    void getPendingSongsPaginated_ShouldThrowException_WhenNotAdmin() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        
        // Mock regular user (not admin)
        testUser.setRole(Role.USER);
        when(authentication.getPrincipal()).thenReturn(testUser);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            songService.getPendingSongsPaginated(pageable);
        });
        
        verify(songRepository, never()).findByStatus(any(), any());
    }

    @Test
    void getAllSongsAdminPaginated_ShouldThrowException_WhenNotAdmin() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        
        // Mock regular user (not admin)
        testUser.setRole(Role.USER);
        when(authentication.getPrincipal()).thenReturn(testUser);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            songService.getAllSongsAdminPaginated(pageable);
        });
        
        verify(songRepository, never()).findAll(any(Pageable.class));
    }
}