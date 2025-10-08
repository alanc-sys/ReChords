package com.misacordes.application.services.auth;

import com.misacordes.application.dto.request.ChordPosition;
import com.misacordes.application.dto.response.SongResponse;
import com.misacordes.application.entities.ChordCatalog;
import com.misacordes.application.entities.ChordCategory;
import com.misacordes.application.entities.DifficultyLevel;
import com.misacordes.application.entities.Role;
import com.misacordes.application.entities.Song;
import com.misacordes.application.entities.SongChord;
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
class SongChordServiceTest {

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
    private ChordCatalog testChord;
    private List<ChordPosition> testChordPositions;

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

        testChord = ChordCatalog.builder()
                .id(1L)
                .name("C")
                .fullName("Do mayor")
                .category(ChordCategory.MAJOR)
                .difficultyLevel(DifficultyLevel.BEGINNER)
                .isCommon(true)
                .displayOrder(1)
                .build();

        testChordPositions = Arrays.asList(
                new ChordPosition("C", 0, 1, 0),
                new ChordPosition("Am", 10, 12, 0),
                new ChordPosition("F", 0, 1, 1)
        );

        // Configurar el contexto de seguridad
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
    }

    @Test
    void testUpdateChordPositions_Success() {
        // Arrange
        when(songRepository.findByIdAndCreatedById(1L, 1L)).thenReturn(Optional.of(testSong));
        when(chordCatalogRepository.findByName("C")).thenReturn(Optional.of(testChord));
        when(chordCatalogRepository.findByName("Am")).thenReturn(Optional.of(testChord));
        when(chordCatalogRepository.findByName("F")).thenReturn(Optional.of(testChord));

        // Act
        SongResponse response = songService.updateChordPositions(1L, testChordPositions);

        // Assert
        assertNotNull(response);
        assertEquals(testSong.getId(), response.getId());
        assertEquals(testSong.getTitle(), response.getTitle());
        
        // Verificar que se eliminaron los acordes existentes
        verify(songChordRepository).deleteBySongId(1L);
        
        // Verificar que se guardaron los nuevos acordes
        verify(songChordRepository, times(3)).save(any(SongChord.class));
    }

    @Test
    void testUpdateChordPositions_SongNotFound() {
        // Arrange
        when(songRepository.findByIdAndCreatedById(1L, 1L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            songService.updateChordPositions(1L, testChordPositions);
        });

        assertEquals("Canción no encontrada", exception.getMessage());
        verify(songChordRepository, never()).deleteBySongId(anyLong());
        verify(songChordRepository, never()).save(any(SongChord.class));
    }

    @Test
    void testUpdateChordPositions_SongNotEditable() {
        // Arrange
        testSong.setStatus(SongStatus.APPROVED);
        when(songRepository.findByIdAndCreatedById(1L, 1L)).thenReturn(Optional.of(testSong));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            songService.updateChordPositions(1L, testChordPositions);
        });

        assertEquals("No puedes editar una canción en estado APPROVED", exception.getMessage());
        verify(songChordRepository, never()).deleteBySongId(anyLong());
        verify(songChordRepository, never()).save(any(SongChord.class));
    }

    @Test
    void testUpdateChordPositions_ChordNotFound() {
        // Arrange
        when(songRepository.findByIdAndCreatedById(1L, 1L)).thenReturn(Optional.of(testSong));
        when(chordCatalogRepository.findByName("C")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            songService.updateChordPositions(1L, testChordPositions);
        });

        assertEquals("Chord not found: C", exception.getMessage());
        verify(songChordRepository).deleteBySongId(1L);
        verify(songChordRepository, never()).save(any(SongChord.class));
    }

    @Test
    void testUpdateChordPositions_EmptyList() {
        // Arrange
        when(songRepository.findByIdAndCreatedById(1L, 1L)).thenReturn(Optional.of(testSong));

        // Act
        SongResponse response = songService.updateChordPositions(1L, Arrays.asList());

        // Assert
        assertNotNull(response);
        assertEquals(testSong.getId(), response.getId());
        
        // Verificar que se eliminaron los acordes existentes
        verify(songChordRepository).deleteBySongId(1L);
        
        // Verificar que no se guardaron nuevos acordes
        verify(songChordRepository, never()).save(any(SongChord.class));
    }

    @Test
    void testUpdateChordPositions_NullList() {
        // Arrange
        when(songRepository.findByIdAndCreatedById(1L, 1L)).thenReturn(Optional.of(testSong));

        // Act
        SongResponse response = songService.updateChordPositions(1L, null);

        // Assert
        assertNotNull(response);
        assertEquals(testSong.getId(), response.getId());
        
        // Verificar que se eliminaron los acordes existentes
        verify(songChordRepository).deleteBySongId(1L);
        
        // Verificar que no se guardaron nuevos acordes
        verify(songChordRepository, never()).save(any(SongChord.class));
    }

    @Test
    void testGetChordPositionsForSong() {
        // Arrange
        when(songRepository.findById(1L)).thenReturn(Optional.of(testSong));
        
        SongChord songChord1 = SongChord.builder()
                .id(1L)
                .song(testSong)
                .chord(testChord)
                .chordName("C")
                .positionStart(0)
                .positionEnd(1)
                .lineNumber(0)
                .build();

        SongChord songChord2 = SongChord.builder()
                .id(2L)
                .song(testSong)
                .chord(testChord)
                .chordName("Am")
                .positionStart(10)
                .positionEnd(12)
                .lineNumber(0)
                .build();

        List<SongChord> songChords = Arrays.asList(songChord1, songChord2);
        when(songChordRepository.findBySongIdOrderByLineNumberAscPositionStartAsc(1L))
                .thenReturn(songChords);

        // Act
        SongResponse response = songService.getSongById(1L);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getChordPositions());
        assertEquals(2, response.getChordPositions().size());
        
        ChordPosition chordPos1 = response.getChordPositions().get(0);
        assertEquals("C", chordPos1.getChordName());
        assertEquals(0, chordPos1.getStartPos());
        assertEquals(1, chordPos1.getEndPos());
        assertEquals(0, chordPos1.getLineNumber());
        
        ChordPosition chordPos2 = response.getChordPositions().get(1);
        assertEquals("Am", chordPos2.getChordName());
        assertEquals(10, chordPos2.getStartPos());
        assertEquals(12, chordPos2.getEndPos());
        assertEquals(0, chordPos2.getLineNumber());
    }
}
