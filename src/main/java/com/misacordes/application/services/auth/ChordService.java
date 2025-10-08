package com.misacordes.application.services.auth;

import com.misacordes.application.dto.response.ChordInfo;
import com.misacordes.application.dto.response.ChordResponse;
import com.misacordes.application.dto.request.ChordRequest;
import com.misacordes.application.entities.ChordCatalog;
import com.misacordes.application.entities.ChordCategory;
import com.misacordes.application.entities.DifficultyLevel;
import com.misacordes.application.entities.Role;
import com.misacordes.application.entities.User;
import com.misacordes.application.repositories.ChordCatalogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChordService {

    private final ChordCatalogRepository chordCatalogRepository;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (User) auth.getPrincipal();
    }

    private void verifyAdmin() {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != Role.ADMIN) {
            throw new RuntimeException("No tienes permisos de administrador");
        }
    }

    // ========== CONSULTAS PÚBLICAS ==========

    public List<ChordResponse> getAllChords() {
        List<ChordCatalog> chords = chordCatalogRepository.findAllByOrderByDisplayOrderAsc();
        return chords.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ChordResponse> getCommonChords() {
        List<ChordCatalog> chords = chordCatalogRepository.findByIsCommonTrueOrderByDisplayOrderAsc();
        return chords.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ChordResponse> getChordsByCategory(ChordCategory category) {
        List<ChordCatalog> chords = chordCatalogRepository.findByCategoryOrderByDisplayOrderAsc(category);
        return chords.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ChordResponse> getChordsByDifficulty(DifficultyLevel level) {
        List<ChordCatalog> chords = chordCatalogRepository.findByDifficultyLevelOrderByDisplayOrderAsc(level);
        return chords.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ChordResponse getChordById(Long id) {
        ChordCatalog chord = chordCatalogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Acorde no encontrado"));
        return mapToResponse(chord);
    }

    // ========== GESTIÓN ADMIN ==========

    public ChordResponse createChord(ChordRequest request) {
        verifyAdmin();

        if (chordCatalogRepository.findByName(request.getName()).isPresent()) {
            throw new RuntimeException("Ya existe un acorde con ese nombre");
        }

        ChordCatalog chord = ChordCatalog.builder()
                .name(request.getName())
                .fullName(request.getFullName())
                .category(request.getCategory())
                .displayOrder(request.getDisplayOrder())
                .isCommon(request.getIsCommon() != null ? request.getIsCommon() : false)
                .difficultyLevel(request.getDifficultyLevel())
                .fingerPositions(request.getFingerPositions())
                .notes(request.getNotes())
                .build();

        ChordCatalog saved = chordCatalogRepository.save(chord);
        return mapToResponse(saved);
    }

    // Actualizar acorde
    public ChordResponse updateChord(Long id, ChordRequest request) {
        verifyAdmin();

        ChordCatalog chord = chordCatalogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Acorde no encontrado"));

        // Verificar que el nombre no esté tomado por otro acorde
        chordCatalogRepository.findByName(request.getName())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new RuntimeException("Ya existe otro acorde con ese nombre");
                    }
                });

        chord.setName(request.getName());
        chord.setFullName(request.getFullName());
        chord.setCategory(request.getCategory());
        chord.setDisplayOrder(request.getDisplayOrder());
        chord.setIsCommon(request.getIsCommon() != null ? request.getIsCommon() : false);
        chord.setDifficultyLevel(request.getDifficultyLevel());
        chord.setFingerPositions(request.getFingerPositions());
        chord.setNotes(request.getNotes());

        ChordCatalog updated = chordCatalogRepository.save(chord);
        return mapToResponse(updated);
    }

    public void deleteChord(Long id) {
        verifyAdmin();

        ChordCatalog chord = chordCatalogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Acorde no encontrado"));

        chordCatalogRepository.delete(chord);
    }


    private ChordResponse mapToResponse(ChordCatalog chord) {
        return ChordResponse.builder()
                .id(chord.getId())
                .name(chord.getName())
                .fullName(chord.getFullName())
                .category(chord.getCategory())
                .displayOrder(chord.getDisplayOrder())
                .isCommon(chord.getIsCommon())
                .difficultyLevel(chord.getDifficultyLevel())
                .fingerPositions(chord.getFingerPositions())
                .build();
    }
    
    // ========== MÉTODOS PARA SELECCIÓN DE ACORDES ==========
    
    /**
     * Obtener todos los acordes disponibles para selección (drag & drop)
     */
    public List<ChordInfo> getAllChordsForSelection() {
        List<ChordCatalog> chords = chordCatalogRepository.findAllByOrderByDisplayOrderAsc();
        return chords.stream()
                .map(this::mapToChordInfo)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtener solo acordes comunes para selección rápida
     */
    public List<ChordInfo> getCommonChordsForSelection() {
        List<ChordCatalog> chords = chordCatalogRepository.findByIsCommonTrueOrderByDisplayOrderAsc();
        return chords.stream()
                .map(this::mapToChordInfo)
                .collect(Collectors.toList());
    }
    
    /**
     * Mapear ChordCatalog a ChordInfo
     */
    private ChordInfo mapToChordInfo(ChordCatalog chord) {
        return ChordInfo.builder()
                .id(chord.getId())
                .name(chord.getName())
                .fullName(chord.getFullName())
                .fingerPositions(chord.getFingerPositions())
                .difficulty(chord.getDifficultyLevel())
                .isCommon(chord.getIsCommon())
                .displayOrder(chord.getDisplayOrder())
                .build();
    }
}
