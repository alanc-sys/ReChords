package com.misacordes.application.services.auth;

import com.misacordes.application.dto.request.ChordRequest;
import com.misacordes.application.dto.response.ChordInfo;
import com.misacordes.application.dto.response.ChordResponse;
import com.misacordes.application.entities.ChordCatalog;
import com.misacordes.application.entities.ChordCategory;
import com.misacordes.application.entities.DifficultyLevel;
import com.misacordes.application.entities.Role;
import com.misacordes.application.entities.User;
import com.misacordes.application.repositories.ChordCatalogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChordServiceTest {

    @Mock
    private ChordCatalogRepository chordCatalogRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ChordService chordService;

    private User testUser;
    private ChordCatalog testChord;
    private ChordRequest chordRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("password123")
                .firstname("Test")
                .lastname("User")
                .country("Spain")
                .role(Role.ADMIN)
                .build();

        testChord = ChordCatalog.builder()
                .id(1L)
                .name("C")
                .fullName("Do mayor")
                .category(ChordCategory.MAJOR)
                .difficultyLevel(DifficultyLevel.BEGINNER)
                .isCommon(true)
                .displayOrder(1)
                .fingerPositions("x32010")
                .notes("C-E-G")
                .build();

        chordRequest = ChordRequest.builder()
                .name("D")
                .fullName("Re mayor")
                .category(ChordCategory.MAJOR)
                .difficultyLevel(DifficultyLevel.BEGINNER)
                .isCommon(true)
                .displayOrder(2)
                .fingerPositions("xx0232")
                .notes("D-F#-A")
                .build();

        // Configurar el contexto de seguridad
        SecurityContextHolder.setContext(securityContext);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getPrincipal()).thenReturn(testUser);
    }

    @Test
    void testGetAllChordsForSelection_Success() {
        // Arrange
        List<ChordCatalog> chords = Arrays.asList(testChord);
        when(chordCatalogRepository.findAllByOrderByDisplayOrderAsc()).thenReturn(chords);

        // Act
        List<ChordInfo> result = chordService.getAllChordsForSelection();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        
        ChordInfo chordInfo = result.get(0);
        assertEquals(testChord.getId(), chordInfo.getId());
        assertEquals(testChord.getName(), chordInfo.getName());
        assertEquals(testChord.getFullName(), chordInfo.getFullName());
        assertEquals(testChord.getFingerPositions(), chordInfo.getFingerPositions());
        assertEquals(testChord.getDifficultyLevel(), chordInfo.getDifficulty());
        assertEquals(testChord.getIsCommon(), chordInfo.getIsCommon());
        assertEquals(testChord.getDisplayOrder(), chordInfo.getDisplayOrder());
    }

    @Test
    void testGetCommonChordsForSelection_Success() {
        // Arrange
        List<ChordCatalog> chords = Arrays.asList(testChord);
        when(chordCatalogRepository.findByIsCommonTrueOrderByDisplayOrderAsc()).thenReturn(chords);

        // Act
        List<ChordInfo> result = chordService.getCommonChordsForSelection();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        
        ChordInfo chordInfo = result.get(0);
        assertEquals(testChord.getId(), chordInfo.getId());
        assertEquals(testChord.getName(), chordInfo.getName());
        assertEquals(testChord.getFullName(), chordInfo.getFullName());
        assertTrue(chordInfo.getIsCommon());
    }

    @Test
    void testGetAllChordsForSelection_EmptyList() {
        // Arrange
        when(chordCatalogRepository.findAllByOrderByDisplayOrderAsc()).thenReturn(Arrays.asList());

        // Act
        List<ChordInfo> result = chordService.getAllChordsForSelection();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetCommonChordsForSelection_EmptyList() {
        // Arrange
        when(chordCatalogRepository.findByIsCommonTrueOrderByDisplayOrderAsc()).thenReturn(Arrays.asList());

        // Act
        List<ChordInfo> result = chordService.getCommonChordsForSelection();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testCreateChord_Success() {
        // Arrange
        when(chordCatalogRepository.findByName("D")).thenReturn(Optional.empty());
        when(chordCatalogRepository.save(any(ChordCatalog.class))).thenReturn(testChord);

        // Act
        ChordResponse result = chordService.createChord(chordRequest);

        // Assert
        assertNotNull(result);
        assertEquals(testChord.getId(), result.getId());
        assertEquals(testChord.getName(), result.getName());
        assertEquals(testChord.getFullName(), result.getFullName());
        assertEquals(testChord.getCategory(), result.getCategory());
        assertEquals(testChord.getDifficultyLevel(), result.getDifficultyLevel());
        assertEquals(testChord.getIsCommon(), result.getIsCommon());
        assertEquals(testChord.getDisplayOrder(), result.getDisplayOrder());
        assertEquals(testChord.getFingerPositions(), result.getFingerPositions());

        verify(chordCatalogRepository).save(any(ChordCatalog.class));
    }

    @Test
    void testCreateChord_AlreadyExists() {
        // Arrange
        when(chordCatalogRepository.findByName("D")).thenReturn(Optional.of(testChord));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            chordService.createChord(chordRequest);
        });

        assertEquals("Ya existe un acorde con ese nombre", exception.getMessage());
        verify(chordCatalogRepository, never()).save(any(ChordCatalog.class));
    }

    @Test
    void testCreateChord_NotAdmin() {
        // Arrange
        testUser.setRole(Role.USER);
        when(authentication.getPrincipal()).thenReturn(testUser);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            chordService.createChord(chordRequest);
        });

        assertEquals("No tienes permisos de administrador", exception.getMessage());
        verify(chordCatalogRepository, never()).findByName(anyString());
        verify(chordCatalogRepository, never()).save(any(ChordCatalog.class));
    }

    @Test
    void testGetChordById_Success() {
        // Arrange
        when(chordCatalogRepository.findById(1L)).thenReturn(Optional.of(testChord));

        // Act
        ChordResponse result = chordService.getChordById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testChord.getId(), result.getId());
        assertEquals(testChord.getName(), result.getName());
        assertEquals(testChord.getFullName(), result.getFullName());
        assertEquals(testChord.getCategory(), result.getCategory());
        assertEquals(testChord.getDifficultyLevel(), result.getDifficultyLevel());
        assertEquals(testChord.getIsCommon(), result.getIsCommon());
        assertEquals(testChord.getDisplayOrder(), result.getDisplayOrder());
        assertEquals(testChord.getFingerPositions(), result.getFingerPositions());
    }

    @Test
    void testGetChordById_NotFound() {
        // Arrange
        when(chordCatalogRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            chordService.getChordById(1L);
        });

        assertEquals("Acorde no encontrado", exception.getMessage());
    }
}
