package com.misacordes.application.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.misacordes.application.dto.request.ChordPositionInfo;
import com.misacordes.application.dto.request.LineWithChords;
import com.misacordes.application.dto.request.SongWithChordsRequest;
import com.misacordes.application.dto.response.AdminStatsResponse;
import com.misacordes.application.dto.response.PageResponse;
import com.misacordes.application.dto.response.SongWithChordsResponse;
import com.misacordes.application.entities.*;
import com.misacordes.application.dto.request.ProposedChordRequest;
import com.misacordes.application.repositories.SongRepository;
import com.misacordes.application.repositories.PlaylistSongRepository;
import com.misacordes.application.repositories.UserRepository;
import com.misacordes.application.repositories.ProposedChordRepository;
import com.misacordes.application.repositories.ChordCatalogRepository;
import com.misacordes.application.utils.ChordTransposer;
import com.misacordes.application.utils.SongStatus;
import com.misacordes.application.config.GlobalExceptionHandler.ResourceNotFoundException;
import com.misacordes.application.config.GlobalExceptionHandler.BusinessException;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class SongService extends BaseService {

    private final SongRepository songRepository;
    private final UserRepository userRepository;
    private final SongAnalyticsService songAnalyticsService;
    private final SongAnalyticsAsyncService songAnalyticsAsyncService;
    private final ObjectMapper objectMapper;
    private final ProposedChordRepository proposedChordRepository;
    private final ChordCatalogRepository chordCatalogRepository;
    private final com.misacordes.application.repositories.DeletionRequestRepository deletionRequestRepository;
    private final PlaylistSongRepository playlistSongRepository;




    public SongWithChordsResponse submitForApprovalWithChords(Long id) {
        User currentUser = getCurrentUser();
        Song song = songRepository.findByIdAndCreatedById(id, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Canción no encontrada"));

        if (song.getStatus() != SongStatus.DRAFT && song.getStatus() != SongStatus.REJECTED) {
            throw new BusinessException("Solo puedes enviar canciones en borrador o rechazadas");
        }
        song.setStatus(SongStatus.PENDING);
        song.setRejectionReason(null);

        Song updated = songRepository.save(song);
        return mapToSongWithChordsResponse(updated);
    }

    // ========== ADMIN ==========

    @Transactional
    public SongWithChordsResponse approveSong(Long id) {
        verifyAdmin();
        User admin = getCurrentUser();
        
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Canción no encontrada"));

        if (song.getStatus() != SongStatus.PENDING) {
            throw new BusinessException("Solo puedes aprobar canciones pendientes");
        }

        song.setStatus(SongStatus.APPROVED);
        song.setIsPublic(true);
        song.setPublishedAt(LocalDateTime.now());

        Song updated = songRepository.save(song);
        
        // Aprobar y agregar los acordes propuestos al catálogo
        approveProposedChords(song, admin);
        
        return mapToSongWithChordsResponse(updated);
    }
    
    /**
     * Aprobar acordes propuestos y agregarlos al catálogo
     */
    private void approveProposedChords(Song song, User admin) {
        List<ProposedChord> proposedChords = proposedChordRepository.findBySong(song);
        
        for (ProposedChord proposedChord : proposedChords) {
            if (proposedChord.getStatus() == ProposalStatus.PENDING) {
                // Verificar si el acorde ya existe en el catálogo
                if (chordCatalogRepository.findByName(proposedChord.getName()).isEmpty()) {
                    // Agregar al catálogo
                    ChordCatalog newChord = ChordCatalog.builder()
                            .name(proposedChord.getName())
                            .fullName(proposedChord.getFullName())
                            .category(proposedChord.getCategory())
                            .fingerPositions(proposedChord.getFingerPositions())
                            .notes(proposedChord.getNotes())
                            .isCommon(false) // Los acordes propuestos no son comunes por defecto
                            .difficultyLevel(DifficultyLevel.INTERMEDIATE)
                            .build();
                    
                    ChordCatalog savedChord = chordCatalogRepository.save(newChord);
                    
                    // Actualizar la propuesta
                    proposedChord.setStatus(ProposalStatus.APPROVED);
                    proposedChord.setCatalogChordId(savedChord.getId());
                } else {
                    // Si ya existe, solo marcar como aprobado
                    proposedChord.setStatus(ProposalStatus.APPROVED);
                    ChordCatalog existingChord = chordCatalogRepository.findByName(proposedChord.getName()).get();
                    proposedChord.setCatalogChordId(existingChord.getId());
                }
                
                proposedChord.setReviewedAt(LocalDateTime.now());
                proposedChord.setReviewedBy(admin);
                proposedChordRepository.save(proposedChord);
            }
        }
    }

    public SongWithChordsResponse rejectSong(Long id, String reason) {
        verifyAdmin();
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Canción no encontrada"));

        if (song.getStatus() != SongStatus.PENDING) {
            throw new BusinessException("Solo puedes rechazar canciones pendientes");
        }

        song.setStatus(SongStatus.REJECTED);
        song.setRejectionReason(reason);

        Song updated = songRepository.save(song);
        return mapToSongWithChordsResponse(updated);
    }

    private boolean canUserViewSong(User user, Song song) {
        if (user.getRole() == Role.ADMIN) {
            return true;
        }
        if (song.getCreatedBy().getId() == user.getId()) {
            return true;
        }

        return song.getIsPublic() && song.getStatus() == SongStatus.APPROVED;
    }

    public SongWithChordsResponse unpublishSong(Long id) {
        verifyAdmin();
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Canción no encontrada"));

        if (song.getStatus() != SongStatus.APPROVED) {
            throw new BusinessException("Solo puedes despublicar canciones aprobadas");
        }

        song.setStatus(SongStatus.DRAFT);
        song.setIsPublic(false);
        song.setPublishedAt(null);

        Song updated = songRepository.save(song);
        return mapToSongWithChordsResponse(updated);
    }

    @Transactional
    public void deleteSongAdmin(Long id) {
        verifyAdmin();
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Canción no encontrada"));

        // Borrar dependencias para evitar violaciones de FK
        proposedChordRepository.deleteBySongId(id);
        deletionRequestRepository.deleteBySongId(id);
        playlistSongRepository.deleteBySongId(id);

        songRepository.delete(song);
    }

    public AdminStatsResponse getAdminStats() {
        verifyAdmin();

        return AdminStatsResponse.builder()
                .totalSongs(songRepository.count())
                .draftSongs(songRepository.countByStatus(SongStatus.DRAFT))
                .pendingSongs(songRepository.countByStatus(SongStatus.PENDING))
                .approvedSongs(songRepository.countByStatus(SongStatus.APPROVED))
                .rejectedSongs(songRepository.countByStatus(SongStatus.REJECTED))
                .totalUsers(userRepository.count())
                .build();
    }

    public SongWithChordsResponse createSongWithChords(SongWithChordsRequest request) {
        User currentUser = getCurrentUser();

        if (!songAnalyticsService.validateChordsMap(convertToJson(request))) {
            throw new BusinessException("Formato de acordes inválido");
        }
        
        Song song = Song.builder()
                .title(request.getTitle())
                .artist(request.getArtist())
                .album(request.getAlbum())
                .year(request.getYear())
                .key(request.getKey())
                .tempo(request.getTempo())
                // Enlaces multimedia
                .youtubeUrl(request.getYoutubeUrl())
                .spotifyUrl(request.getSpotifyUrl())
                // Personalización de portada
                .coverImageUrl(request.getCoverImageUrl())
                .coverColor(request.getCoverColor())
                .createdBy(currentUser)
                .chordsMap(convertToJson(request))
                .status(SongStatus.DRAFT)
                .isPublic(false)
                .build();

        Song savedSong = songRepository.save(song);

        // Guardar acordes propuestos si existen
        if (request.getProposedChords() != null && !request.getProposedChords().isEmpty()) {
            saveProposedChords(request.getProposedChords(), savedSong, currentUser);
        }

        processSongAnalyticsAsync(savedSong);
        
        return mapToSongWithChordsResponse(savedSong);
    }
    
    /**
     * Guardar acordes propuestos por el usuario
     */
    private void saveProposedChords(List<ProposedChordRequest> proposedChords, Song song, User user) {
        for (ProposedChordRequest chordRequest : proposedChords) {
            // Determinar la categoría del acorde
            ChordCategory category = determineChordCategory(chordRequest.getCategory());
            
            ProposedChord proposedChord = ProposedChord.builder()
                    .name(chordRequest.getName())
                    .fullName(chordRequest.getFullName())
                    .category(category)
                    .fingerPositions(chordRequest.getFingerPositions())
                    .notes(chordRequest.getNotes())
                    .proposedBy(user)
                    .song(song)
                    .status(ProposalStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .build();
            
            proposedChordRepository.save(proposedChord);
        }
    }
    
    /**
     * Determinar la categoría del acorde basándose en el string recibido
     */
    private ChordCategory determineChordCategory(String categoryStr) {
        if (categoryStr == null || categoryStr.isEmpty()) {
            return ChordCategory.OTHER;
        }
        
        try {
            return ChordCategory.valueOf(categoryStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ChordCategory.OTHER;
        }
    }

    public SongWithChordsResponse getSongWithChordsById(Long id) {
        User currentUser = getCurrentUser();
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Song not found"));

        if (!canUserViewSong(currentUser, song)) {
            throw new BusinessException("No tienes permiso para ver esta canción");
        }
        
        return mapToSongWithChordsResponse(song);
    }

    public SongWithChordsResponse updateSongWithChords(Long id, SongWithChordsRequest request) {
        User currentUser = getCurrentUser();
        
        // findByIdAndCreatedById ya verifica que el usuario sea el creador
        Song song = songRepository.findByIdAndCreatedById(id, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Canción no encontrada o no tienes permisos para editarla"));

        if (!songAnalyticsService.validateChordsMap(convertToJson(request))) {
            throw new BusinessException("Formato de acordes inválido");
        }

        // Actualizar campos básicos
        song.setTitle(request.getTitle());
        song.setArtist(request.getArtist());
        song.setAlbum(request.getAlbum());
        song.setYear(request.getYear());
        song.setKey(request.getKey());
        song.setTempo(request.getTempo());
        
        // Actualizar enlaces multimedia
        song.setYoutubeUrl(request.getYoutubeUrl());
        song.setSpotifyUrl(request.getSpotifyUrl());
        
        // Actualizar personalización de portada
        song.setCoverImageUrl(request.getCoverImageUrl());
        song.setCoverColor(request.getCoverColor());
        
        song.setChordsMap(convertToJson(request));

        // Lógica de estados:
        // - DRAFT o REJECTED: se mantiene el estado
        // - APPROVED: pasa a PENDING (requiere nueva aprobación)
        // - PENDING: se mantiene PENDING
        if (song.getStatus() == SongStatus.APPROVED) {
            song.setStatus(SongStatus.PENDING);
            song.setPublishedAt(null); // Ya no está publicada hasta nueva aprobación
            System.out.println("⚠️ Canción APROBADA editada por creador. Cambiando a PENDING para nueva revisión.");
        }
        
        if (song.getStatus() == SongStatus.REJECTED) {
            song.setRejectionReason(null);
            song.setStatus(SongStatus.DRAFT); // Vuelve a borrador
        }

        Song updated = songRepository.save(song);

        // Guardar acordes propuestos si existen
        if (request.getProposedChords() != null && !request.getProposedChords().isEmpty()) {
            saveProposedChords(request.getProposedChords(), updated, currentUser);
        }

        processSongAnalyticsAsync(updated);
        
        return mapToSongWithChordsResponse(updated);
    }
    public com.misacordes.application.dto.response.SongAnalyticsResponse getSongAnalytics(Long id) {
        User currentUser = getCurrentUser();
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Song not found"));

        if (!canUserViewSong(currentUser, song)) {
            throw new BusinessException("No tienes permiso para ver esta canción");
        }
        
        return songAnalyticsService.analyzeSongChords(song);
    }

    private String convertToJson(SongWithChordsRequest request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new BusinessException("Error converting request to JSON: " + e.getMessage());
        }
    }

    public PageResponse<SongWithChordsResponse> getMySongsWithChordsPaginated(Pageable pageable) {
        User currentUser = getCurrentUser();
        Page<Song> songsPage = songRepository.findByCreatedById(currentUser.getId(), pageable);

        Page<SongWithChordsResponse> responsePage = songsPage.map(this::mapToSongWithChordsResponse);

        return PageResponse.from(responsePage);
    }

    public PageResponse<SongWithChordsResponse> getPublicSongsWithChordsPaginated(Pageable pageable) {
        Page<Song> songsPage = songRepository.findByIsPublicTrueAndStatus(SongStatus.APPROVED, pageable);

        Page<SongWithChordsResponse> responsePage = songsPage.map(this::mapToSongWithChordsResponse);

        return PageResponse.from(responsePage);
    }

    public PageResponse<SongWithChordsResponse> searchPublicSongsWithChordsPaginated(String query, Pageable pageable) {
        Page<Song> songsPage = songRepository
                .findByIsPublicTrueAndStatusAndTitleContainingIgnoreCaseOrArtistContainingIgnoreCase(
                        SongStatus.APPROVED, query, query, pageable);

        Page<SongWithChordsResponse> responsePage = songsPage.map(this::mapToSongWithChordsResponse);

        return PageResponse.from(responsePage);
    }

    public PageResponse<SongWithChordsResponse> getPendingSongsPaginated(Pageable pageable) {
        verifyAdmin();
        Page<Song> songsPage = songRepository.findByStatus(SongStatus.PENDING, pageable);

        Page<SongWithChordsResponse> responsePage = songsPage.map(this::mapToSongWithChordsResponse);

        return PageResponse.from(responsePage);
    }

    public PageResponse<SongWithChordsResponse> getAllSongsAdminPaginated(Pageable pageable) {
        verifyAdmin();
        Page<Song> songsPage = songRepository.findAll(pageable);

        Page<SongWithChordsResponse> responsePage = songsPage.map(this::mapToSongWithChordsResponse);

        return PageResponse.from(responsePage);
    }

    private SongWithChordsResponse mapToSongWithChordsResponse(Song song) {
        try {
            SongWithChordsRequest songData = null;
            if (song.getChordsMap() != null && !song.getChordsMap().trim().isEmpty()) {
                songData = objectMapper.readValue(
                    song.getChordsMap(), 
                    new TypeReference<SongWithChordsRequest>() {}
                );
            }
            
            return SongWithChordsResponse.builder()
                .id(song.getId())
                .title(song.getTitle())
                .artist(song.getArtist())
                .album(song.getAlbum())
                .year(song.getYear())
                    .key(song.getKey())
                    .tempo(song.getTempo())
                    // Enlaces multimedia
                    .youtubeUrl(song.getYoutubeUrl())
                    .spotifyUrl(song.getSpotifyUrl())
                    .youtubeVideoId(song.getYoutubeVideoId())
                    .spotifyTrackId(song.getSpotifyTrackId())
                    // Personalización
                    .coverImageUrl(song.getCoverImageUrl())
                    .coverColor(song.getCoverColor())
                .status(song.getStatus())
                .isPublic(song.getIsPublic())
                .rejectionReason(song.getRejectionReason())
                .createdAt(song.getCreatedAt())
                .publishedAt(song.getPublishedAt())
                    .lyrics(songData != null ? songData.getLyrics() : null)
                    .createdBy(SongWithChordsResponse.CreatorInfo.builder()
                        .id(song.getCreatedBy().getId())
                        .username(song.getCreatedBy().getUsername())
                        .firstname(song.getCreatedBy().getFirstname())
                        .build())
                    .build();
        } catch (JsonProcessingException e) {
            throw new BusinessException("Error parsing song chords map: " + e.getMessage());
        }
    }

    private void processSongAnalyticsAsync(Song song) {
        songAnalyticsAsyncService.processSongAnalyticsAsync(song.getId());
    }

    @Transactional(readOnly = true)
    public SongWithChordsResponse transposeSong(Long songId, int semitones) {
        SongWithChordsResponse response = getSongWithChordsById(songId);

        if (response.getLyrics() == null) {
            return response;
        }
        List<LineWithChords> transposedLyrics = new ArrayList<>();

        for (LineWithChords line : response.getLyrics()) {
            List<ChordPositionInfo> transposedChords = new ArrayList<>();
            if (line.getChords() != null) {
                for (ChordPositionInfo originalChord : line.getChords()) {
                    String newChordName = ChordTransposer.transpose(originalChord.getName(), semitones);
                    transposedChords.add(new ChordPositionInfo(
                            originalChord.getStart(),
                            newChordName,
                            null
                    ));
                }
            }
            transposedLyrics.add(new LineWithChords(line.getLineNumber(), line.getText(), transposedChords));
        }

        response.setLyrics(transposedLyrics);
        if (response.getKey() != null) {
            response.setKey(ChordTransposer.transpose(response.getKey(), semitones));
        }

        return response;
    }

    /**
     * Eliminar canción directamente (solo si NO está APPROVED)
     */
    @Transactional
    public void deleteSong(Long id) {
        User currentUser = getCurrentUser();
        Song song = songRepository.findByIdAndCreatedById(id, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Canción no encontrada o no tienes permisos para eliminarla"));
        
        // Verificar que la canción NO esté aprobada
        if (song.getStatus() == SongStatus.APPROVED) {
            throw new BusinessException("No puedes eliminar una canción aprobada. Debes solicitar su eliminación al administrador.");
        }
        
        // Eliminar dependencias primero
        proposedChordRepository.deleteBySongId(id);
        deletionRequestRepository.deleteBySongId(id);
        playlistSongRepository.deleteBySongId(id);

        // Eliminar la canción
        songRepository.delete(song);
        System.out.println("✅ Canción eliminada: " + song.getTitle() + " (ID: " + id + ") por usuario: " + currentUser.getUsername());
    }

    /**
     * Solicitar eliminación de canción APPROVED al administrador
     */
    @Transactional
    public DeletionRequest requestSongDeletion(Long songId, String reason) {
        User currentUser = getCurrentUser();
        Song song = songRepository.findByIdAndCreatedById(songId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Canción no encontrada o no tienes permisos"));
        
        // Verificar que la canción esté aprobada
        if (song.getStatus() != SongStatus.APPROVED) {
            throw new BusinessException("Solo puedes solicitar la eliminación de canciones aprobadas. Para otras, elimínalas directamente.");
        }
        
        // Verificar si ya existe una solicitud pendiente
        if (deletionRequestRepository.existsBySongAndStatus(song, DeletionStatus.PENDING)) {
            throw new BusinessException("Ya existe una solicitud pendiente de eliminación para esta canción");
        }
        
        // Crear solicitud de eliminación
        DeletionRequest request = DeletionRequest.builder()
                .song(song)
                .requestedBy(currentUser)
                .reason(reason)
                .status(DeletionStatus.PENDING)
                .build();
        
        DeletionRequest saved = deletionRequestRepository.save(request);
        System.out.println("📨 Solicitud de eliminación creada para canción: " + song.getTitle() + " (ID: " + songId + ")");
        
        return saved;
    }

    /**
     * Verificar si existe una solicitud de eliminación pendiente para una canción
     */
    public boolean hasPendingDeletionRequest(Long songId) {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResourceNotFoundException("Canción no encontrada"));
        return deletionRequestRepository.existsBySongAndStatus(song, DeletionStatus.PENDING);
    }

}
