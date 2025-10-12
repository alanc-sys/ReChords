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
import com.misacordes.application.entities.Role;
import com.misacordes.application.entities.Song;
import com.misacordes.application.entities.User;
import com.misacordes.application.repositories.SongRepository;
import com.misacordes.application.repositories.UserRepository;
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

    public SongWithChordsResponse approveSong(Long id) {
        verifyAdmin();
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Canción no encontrada"));

        if (song.getStatus() != SongStatus.PENDING) {
            throw new BusinessException("Solo puedes aprobar canciones pendientes");
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


    public void deleteSong(Long id) {
        User currentUser = getCurrentUser();
        Song song = songRepository.findByIdAndCreatedById(id, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Canción no encontrada"));

        if (song.getStatus() != SongStatus.DRAFT) {
            throw new BusinessException("Solo puedes eliminar canciones en borrador");
        }

        songRepository.delete(song);
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

    public void deleteSongAdmin(Long id) {
        verifyAdmin();
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Canción no encontrada"));

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
                .createdBy(currentUser)
                .chordsMap(convertToJson(request))
                .status(SongStatus.DRAFT)
                .isPublic(false)
                .build();

        Song savedSong = songRepository.save(song);

        processSongAnalyticsAsync(savedSong);
        
        return mapToSongWithChordsResponse(savedSong);
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
        Song song = songRepository.findByIdAndCreatedById(id, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Canción no encontrada"));
        
        if (song.getStatus() != SongStatus.DRAFT && song.getStatus() != SongStatus.REJECTED) {
            throw new BusinessException("No puedes editar una canción en estado " + song.getStatus());
        }

        if (!songAnalyticsService.validateChordsMap(convertToJson(request))) {
            throw new BusinessException("Formato de acordes inválido");
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

}
