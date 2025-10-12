package com.misacordes.application.controller;


import com.misacordes.application.dto.request.RejectSongRequest;
import com.misacordes.application.dto.response.AdminStatsResponse;
import com.misacordes.application.dto.response.PageResponse;
import com.misacordes.application.dto.response.SongWithChordsResponse;
import com.misacordes.application.services.SongService;
import com.misacordes.application.services.SongAnalyticsAsyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminController {

    private final SongService songService;
    private final SongAnalyticsAsyncService songAnalyticsAsyncService;


    @PutMapping("/songs/{id}/approve")
    public ResponseEntity<SongWithChordsResponse> approveSong(@PathVariable Long id) {
        try {
            SongWithChordsResponse response = songService.approveSong(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new RuntimeException("Error al aprobar canción: " + e.getMessage());
        }
    }

    @PutMapping("/songs/{id}/reject")
    public ResponseEntity<SongWithChordsResponse> rejectSong(
            @PathVariable Long id,
            @RequestBody RejectSongRequest request) {
        try {
            SongWithChordsResponse response = songService.rejectSong(id, request.getReason());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new RuntimeException("Error al rechazar canción: " + e.getMessage());
        }
    }

    @PutMapping("/songs/{id}/unpublish")
    public ResponseEntity<SongWithChordsResponse> unpublishSong(@PathVariable Long id) {
        try {
            SongWithChordsResponse response = songService.unpublishSong(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new RuntimeException("Error al despublicar canción: " + e.getMessage());
        }
    }

    @DeleteMapping("/songs/{id}")
    public ResponseEntity<Void> deleteSongAdmin(@PathVariable Long id) {
        songService.deleteSongAdmin(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponse> getStats() {
        AdminStatsResponse stats = songService.getAdminStats();
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/analytics/process-all")
    public ResponseEntity<String> processAllSongsAnalytics() {
        try {
            songAnalyticsAsyncService.processMultipleSongsAnalyticsAsync();
            return ResponseEntity.ok("Procesamiento masivo de analítica iniciado");
        } catch (Exception e) {
            throw new RuntimeException("Error al iniciar procesamiento masivo: " + e.getMessage());
        }
    }

    @GetMapping("/songs/pending")
    public ResponseEntity<PageResponse<SongWithChordsResponse>> getPendingSongsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort) {
        try {
            Pageable pageable = createPageable(page, size, sort);
            PageResponse<SongWithChordsResponse> response = songService.getPendingSongsPaginated(pageable);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new RuntimeException("Failed to obtain pending songs: " + e.getMessage());
        }
    }

    @GetMapping("/songs")
    public ResponseEntity<PageResponse<SongWithChordsResponse>> getAllSongsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort) {
        try {
            Pageable pageable = createPageable(page, size, sort);
            PageResponse<SongWithChordsResponse> response = songService.getAllSongsAdminPaginated(pageable);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new RuntimeException("Failed to obtain songs: " + e.getMessage());
        }
    }

    private Pageable createPageable(int page, int size, String[] sort) {
        if (size > 100) size = 100;
        if (size < 1) size = 20;

        Sort.Direction direction = Sort.Direction.DESC;
        String property = "createdAt";

        if (sort.length > 0) {
            property = sort[0];
            if (sort.length > 1) {
                direction = Sort.Direction.fromString(sort[1]);
            }
        }

        return PageRequest.of(page, size, Sort.by(direction, property));
    }
}
