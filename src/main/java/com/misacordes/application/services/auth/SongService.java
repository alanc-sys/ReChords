package com.misacordes.application.services.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.misacordes.application.dto.request.ChordPosition;
import com.misacordes.application.dto.request.SongRequest;
import com.misacordes.application.dto.response.AdminStatsResponse;
import com.misacordes.application.dto.response.SongResponse;
import com.misacordes.application.entities.ChordCatalog;
import com.misacordes.application.entities.Role;
import com.misacordes.application.entities.Song;
import com.misacordes.application.entities.SongChord;
import com.misacordes.application.entities.User;
import com.misacordes.application.repositories.ChordCatalogRepository;
import com.misacordes.application.repositories.SongChordRepository;
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
    private final SongChordRepository songChordRepository;
    private final ChordCatalogRepository chordCatalogRepository;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }

    public SongResponse createSong(SongRequest request) {
        User currentUser = getCurrentUser();
        Song song = Song.builder()
                .title(request.getTitle())
                .artist(request.getArtist())
                .album(request.getAlbum())
                .year(request.getYear())
                .createdBy(currentUser)
                .lyricsData(request.getLyricsData())
                .status(SongStatus.DRAFT)
                .isPublic(false)
                .build();

        Song savedSong = songRepository.save(song);
        
        // 🆕 Guardar posiciones de acordes si se proporcionan
        if (request.getChords() != null && !request.getChords().isEmpty()) {
            saveChordPositions(savedSong, request.getChords());
        }
        
        return mapToResponse(savedSong);
    }

    public List<SongResponse> getMySongs() {
        User currentUser = getCurrentUser();
        List<Song> songs = songRepository.findByCreatedById(currentUser.getId());

        return songs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<SongResponse> getPublicSongs() {
        List<Song> songs = songRepository.findByIsPublicTrueAndStatus(SongStatus.APPROVED);

        return songs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public SongResponse getSongById(long id) {
        User currentUser = getCurrentUser();
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Song not found"));

        if (!canUserViewSong(currentUser, song)) {
            throw new RuntimeException("No tienes permiso para ver esta canción");
        }
        return mapToResponse(song);
    }

    public SongResponse updateSong(Long id, SongRequest request) {
        User currentUser = getCurrentUser();
        Song song = songRepository.findByIdAndCreatedById(id, currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Canción no encontrada"));

        if (song.getStatus() != SongStatus.DRAFT && song.getStatus() != SongStatus.REJECTED) {
            throw new RuntimeException("No puedes editar una canción en estado " + song.getStatus());
        }

        song.setTitle(request.getTitle());
        song.setArtist(request.getArtist());
        song.setAlbum(request.getAlbum());
        song.setYear(request.getYear());
        song.setLyricsData(request.getLyricsData());

        if (song.getStatus() == SongStatus.REJECTED) {
            song.setRejectionReason(null);
        }

        Song updated = songRepository.save(song);
        
        // 🆕 Actualizar posiciones de acordes
        updateChordPositions(updated, request.getChords());
        
        return mapToResponse(updated);
    }


    public SongResponse submitForApproval(Long id) {
        User currentUser = getCurrentUser();
        Song song = songRepository.findByIdAndCreatedById(id, currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Canción no encontrada"));

        if (song.getStatus() != SongStatus.DRAFT && song.getStatus() != SongStatus.REJECTED) {
            throw new RuntimeException("Solo puedes enviar canciones en borrador o rechazadas");
        }
        song.setStatus(SongStatus.PENDING);
        song.setRejectionReason(null);

        Song updated = songRepository.save(song);
        return mapToResponse(updated);
    }

    // ========== ADMIN ==========
    public List<SongResponse> getPendingSongs() {
        verifyAdmin();
        List<Song> songs = songRepository.findByStatus(SongStatus.PENDING);

        return songs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public SongResponse approveSong(Long id) {
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
        return mapToResponse(updated);
    }

    public SongResponse rejectSong(Long id, String reason) {
        verifyAdmin();
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Canción no encontrada"));

        if (song.getStatus() != SongStatus.PENDING) {
            throw new RuntimeException("Solo puedes rechazar canciones pendientes");
        }

        song.setStatus(SongStatus.REJECTED);
        song.setRejectionReason(reason);

        Song updated = songRepository.save(song);
        return mapToResponse(updated);
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

    public List<SongResponse> searchPublicSongs(String query) throws JsonProcessingException {
        List<Song> songs = songRepository
                .findByIsPublicTrueAndStatusAndTitleContainingIgnoreCaseOrArtistContainingIgnoreCase(
                        SongStatus.APPROVED, query, query);

        return songs.stream()
                .map(this::mapToResponseSafe)
                .collect(Collectors.toList());
    }

    public List<SongResponse> getAllSongsAdmin() throws JsonProcessingException {
        verifyAdmin();
        List<Song> songs = songRepository.findAll();

        return songs.stream()
                .map(this::mapToResponseSafe)
                .collect(Collectors.toList());
    }

    public SongResponse unpublishSong(Long id) throws JsonProcessingException {
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
        return mapToResponse(updated);
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
                .totalUsers(userRepository.count())  // Necesitas inyectar UserRepository
                .build();
    }

    private SongResponse mapToResponse (Song song){
        return SongResponse.builder()
                .id(song.getId())
                .title(song.getTitle())
                .artist(song.getArtist())
                .album(song.getAlbum())
                .year(song.getYear())
                .lyrics(song.getLyricsData())
                .status(song.getStatus())
                .isPublic(song.getIsPublic())
                .rejectionReason(song.getRejectionReason())
                .createdAt(song.getCreatedAt())
                .publishedAt(song.getPublishedAt())
                .createdBy(SongResponse.CreatorInfo.builder()
                        .id(song.getCreatedBy().getId())
                        .username(song.getCreatedBy().getUsername())
                        .firstname(song.getCreatedBy().getFirstname())
                        .build())
                .chordPositions(getChordPositionsForSong(song.getId()))
                .build();
    }

    private SongResponse mapToResponseSafe (Song song){
        try {
            return mapToResponse(song);
        } catch (Exception e) {
            throw new RuntimeException("Error al procesar canción", e);
        }
    }
    
    // ========== MÉTODOS PARA MANEJO DE ACORDES ==========
    
    /**
     * Guardar posiciones de acordes para una canción
     */
    private void saveChordPositions(Song song, List<ChordPosition> chordPositions) {
        for (ChordPosition chordPos : chordPositions) {
            ChordCatalog chord = chordCatalogRepository.findByName(chordPos.getChordName())
                    .orElseThrow(() -> new RuntimeException("Chord not found: " + chordPos.getChordName()));
            
            SongChord songChord = SongChord.builder()
                    .song(song)
                    .chord(chord)
                    .positionStart(chordPos.getStartPos())
                    .positionEnd(chordPos.getEndPos())
                    .lineNumber(chordPos.getLineNumber())
                    .chordName(chordPos.getChordName())
                    .build();
            
            songChordRepository.save(songChord);
        }
    }
    
    /**
     * Actualizar posiciones de acordes (elimina las existentes y crea las nuevas)
     */
    private void updateChordPositions(Song song, List<ChordPosition> chordPositions) {
        // Eliminar acordes existentes
        songChordRepository.deleteBySongId(song.getId());
        
        // Guardar nuevos acordes si se proporcionan
        if (chordPositions != null && !chordPositions.isEmpty()) {
            saveChordPositions(song, chordPositions);
        }
    }
    
    /**
     * Obtener posiciones de acordes para una canción
     */
    private List<ChordPosition> getChordPositionsForSong(Long songId) {
        List<SongChord> songChords = songChordRepository.findBySongIdOrderByLineNumberAscPositionStartAsc(songId);
        
        return songChords.stream()
                .map(this::mapToChordPosition)
                .collect(Collectors.toList());
    }
    
    /**
     * Mapear SongChord a ChordPosition
     */
    private ChordPosition mapToChordPosition(SongChord songChord) {
        return new ChordPosition(
                songChord.getChordName(),
                songChord.getPositionStart(),
                songChord.getPositionEnd(),
                songChord.getLineNumber()
        );
    }
    
    /**
     * Actualizar solo las posiciones de acordes de una canción
     */
    public SongResponse updateChordPositions(Long songId, List<ChordPosition> chordPositions) {
        User currentUser = getCurrentUser();
        Song song = songRepository.findByIdAndCreatedById(songId, currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Canción no encontrada"));
        
        if (song.getStatus() != SongStatus.DRAFT && song.getStatus() != SongStatus.REJECTED) {
            throw new RuntimeException("No puedes editar una canción en estado " + song.getStatus());
        }
        
        updateChordPositions(song, chordPositions);
        return mapToResponse(song);
    }
}
