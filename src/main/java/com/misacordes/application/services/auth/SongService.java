package com.misacordes.application.services.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.misacordes.application.dto.request.SongWithChordsRequest;
import com.misacordes.application.dto.response.AdminStatsResponse;
import com.misacordes.application.dto.response.SongWithChordsResponse;
import com.misacordes.application.services.SongAnalyticsService;
import com.misacordes.application.services.SongAnalyticsAsyncService;
import com.misacordes.application.entities.Role;
import com.misacordes.application.entities.Song;
import com.misacordes.application.entities.User;
import com.misacordes.application.repositories.SongRepository;
import com.misacordes.application.repositories.UserRepository;
import com.misacordes.application.utils.SongStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class SongService {

    private final SongRepository songRepository;
    private final UserRepository userRepository;
    private final SongAnalyticsService songAnalyticsService;
    private final SongAnalyticsAsyncService songAnalyticsAsyncService;
    private final ObjectMapper objectMapper;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }

    public List<SongWithChordsResponse> getMySongsWithChords() {
        User currentUser = getCurrentUser();
        List<Song> songs = songRepository.findByCreatedById(currentUser.getId());

        return songs.stream()
                .map(this::mapToSongWithChordsResponse)
                .collect(Collectors.toList());
    }

    public List<SongWithChordsResponse> getPublicSongsWithChords() {
        List<Song> songs = songRepository.findByIsPublicTrueAndStatus(SongStatus.APPROVED);

        return songs.stream()
                .map(this::mapToSongWithChordsResponse)
                .collect(Collectors.toList());
    }

    public List<SongWithChordsResponse> searchPublicSongsWithChords(String query) {
        List<Song> songs = songRepository
                .findByIsPublicTrueAndStatusAndTitleContainingIgnoreCaseOrArtistContainingIgnoreCase(
                        SongStatus.APPROVED, query, query);

        return songs.stream()
                .map(this::mapToSongWithChordsResponse)
                .collect(Collectors.toList());
    }

    public SongWithChordsResponse submitForApprovalWithChords(Long id) {
        User currentUser = getCurrentUser();
        Song song = songRepository.findByIdAndCreatedById(id, currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Canción no encontrada"));

        if (song.getStatus() != SongStatus.DRAFT && song.getStatus() != SongStatus.REJECTED) {
            throw new RuntimeException("Solo puedes enviar canciones en borrador o rechazadas");
        }
        song.setStatus(SongStatus.PENDING);
        song.setRejectionReason(null);

        Song updated = songRepository.save(song);
        return mapToSongWithChordsResponse(updated);
    }

    // ========== ADMIN ==========
    public List<SongWithChordsResponse> getPendingSongs() {
        verifyAdmin();
        List<Song> songs = songRepository.findByStatus(SongStatus.PENDING);

        return songs.stream()
                .map(this::mapToSongWithChordsResponse)
                .collect(Collectors.toList());
    }

    public SongWithChordsResponse approveSong(Long id) {
        verifyAdmin();
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Canción no encontrada"));

        if (song.getStatus() != SongStatus.PENDING) {
            throw new RuntimeException("Solo puedes aprobar canciones pendientes");
        }

        song.setStatus(SongStatus.APPROVED);
        song.setIsPublic(true);
        song.setPublishedAt(LocalDateTime.now());

        Song updated = songRepository.save(song);
        return mapToSongWithChordsResponse(updated);
    }

    public SongWithChordsResponse rejectSong(Long id, String reason) {
        verifyAdmin();
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Canción no encontrada"));

        if (song.getStatus() != SongStatus.PENDING) {
            throw new RuntimeException("Solo puedes rechazar canciones pendientes");
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

    private void verifyAdmin() {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != Role.ADMIN) {
            throw new RuntimeException("No tienes permisos de administrador");
        }
    }

    public void deleteSong(Long id) {
        User currentUser = getCurrentUser();
        Song song = songRepository.findByIdAndCreatedById(id, currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Canción no encontrada"));

        if (song.getStatus() != SongStatus.DRAFT) {
            throw new RuntimeException("Solo puedes eliminar canciones en borrador");
        }

        songRepository.delete(song);
    }

    public List<SongWithChordsResponse> getAllSongsAdmin() {
        verifyAdmin();
        List<Song> songs = songRepository.findAll();

        return songs.stream()
                .map(this::mapToSongWithChordsResponse)
                .collect(Collectors.toList());
    }

    public SongWithChordsResponse unpublishSong(Long id) {
        verifyAdmin();
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Canción no encontrada"));

        if (song.getStatus() != SongStatus.APPROVED) {
            throw new RuntimeException("Solo puedes despublicar canciones aprobadas");
        }

        song.setStatus(SongStatus.DRAFT);
        song.setIsPublic(false);
        song.setPublishedAt(null);

        Song updated = songRepository.save(song);
        return mapToSongWithChordsResponse(updated);
    }

    public void deleteSongAdmin(Long id) {
        verifyAdmin();
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Canción no encontrada"));

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

    // ========== MÉTODOS PARA EL NUEVO FORMATO JSON CON ACORDES ==========

    /**
     * Crear una canción con el nuevo formato JSON que incluye letra y acordes
     */
    public SongWithChordsResponse createSongWithChords(SongWithChordsRequest request) {
        User currentUser = getCurrentUser();
        
        // Validar el JSON de acordes
        if (!songAnalyticsService.validateChordsMap(convertToJson(request))) {
            throw new RuntimeException("Formato de acordes inválido");
        }
        
        Song song = Song.builder()
                .title(request.getTitle())
                .artist(request.getArtist())
                .album(request.getAlbum())
                .year(request.getYear())
                .createdBy(currentUser)
                .chordsMap(convertToJson(request))
                .status(SongStatus.DRAFT)
                .isPublic(false)
                .build();

        Song savedSong = songRepository.save(song);
        
        // Procesar asíncronamente para analítica (opcional)
        processSongAnalyticsAsync(savedSong);
        
        return mapToSongWithChordsResponse(savedSong);
    }

    /**
     * Obtener una canción con el formato JSON de acordes
     */
    public SongWithChordsResponse getSongWithChordsById(Long id) {
        User currentUser = getCurrentUser();
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Song not found"));

        if (!canUserViewSong(currentUser, song)) {
            throw new RuntimeException("No tienes permiso para ver esta canción");
        }
        
        return mapToSongWithChordsResponse(song);
    }

    /**
     * Actualizar una canción con el nuevo formato JSON
     */
    public SongWithChordsResponse updateSongWithChords(Long id, SongWithChordsRequest request) {
        User currentUser = getCurrentUser();
        Song song = songRepository.findByIdAndCreatedById(id, currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Canción no encontrada"));
        
        if (song.getStatus() != SongStatus.DRAFT && song.getStatus() != SongStatus.REJECTED) {
            throw new RuntimeException("No puedes editar una canción en estado " + song.getStatus());
        }
        
        // Validar el JSON de acordes
        if (!songAnalyticsService.validateChordsMap(convertToJson(request))) {
            throw new RuntimeException("Formato de acordes inválido");
        }

        song.setTitle(request.getTitle());
        song.setArtist(request.getArtist());
        song.setAlbum(request.getAlbum());
        song.setYear(request.getYear());
        song.setChordsMap(convertToJson(request));

        if (song.getStatus() == SongStatus.REJECTED) {
            song.setRejectionReason(null);
        }

        Song updated = songRepository.save(song);
        
        // Procesar asíncronamente para analítica
        processSongAnalyticsAsync(updated);
        
        return mapToSongWithChordsResponse(updated);
    }

    /**
     * Obtener analítica de una canción
     */
    public com.misacordes.application.dto.response.SongAnalyticsResponse getSongAnalytics(Long id) {
        User currentUser = getCurrentUser();
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Song not found"));

        if (!canUserViewSong(currentUser, song)) {
            throw new RuntimeException("No tienes permiso para ver esta canción");
        }
        
        return songAnalyticsService.analyzeSongChords(song);
    }

    /**
     * Convertir SongWithChordsRequest a JSON string
     */
    private String convertToJson(SongWithChordsRequest request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting request to JSON", e);
        }
    }

    /**
     * Mapear Song a SongWithChordsResponse
     */
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
                    .key(songData != null ? songData.getKey() : null)
                    .tempo(songData != null ? songData.getTempo() : null)
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
            throw new RuntimeException("Error parsing song chords map", e);
        }
    }

    /**
     * Procesar analítica de la canción de forma asíncrona
     */
    private void processSongAnalyticsAsync(Song song) {
        songAnalyticsAsyncService.processSongAnalyticsAsync(song.getId());
    }
}
