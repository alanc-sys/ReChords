package com.misacordes.application.controller;


import com.misacordes.application.dto.request.RejectSongRequest;
import com.misacordes.application.dto.response.AdminStatsResponse;
import com.misacordes.application.dto.response.SongWithChordsResponse;
import com.misacordes.application.services.auth.SongService;
import com.misacordes.application.services.SongAnalyticsAsyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class adminController {

    private final SongService songService;
    private final SongAnalyticsAsyncService songAnalyticsAsyncService;

    @GetMapping("/songs/pending")
    public ResponseEntity<List<SongWithChordsResponse>> getPendingSongs() {
        try {
            List<SongWithChordsResponse> songs = songService.getPendingSongs();
            return ResponseEntity.ok(songs);
        } catch (Exception e) {
            throw new RuntimeException("Failed to obtain Songs: " + e.getMessage());
        }
    }

    @GetMapping("/songs")
    public ResponseEntity<List<SongWithChordsResponse>> getAllSongs() {
        try {
            List<SongWithChordsResponse> songs = songService.getAllSongsAdmin();
            return ResponseEntity.ok(songs);
        } catch (Exception e) {
            throw new RuntimeException("Failed to obtain Songs: " + e.getMessage());
        }
    }

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

    /**
     * POST /api/admin/analytics/process-all
     * Procesar analítica de todas las canciones de forma asíncrona (solo admin)
     */
    @PostMapping("/analytics/process-all")
    public ResponseEntity<String> processAllSongsAnalytics() {
        try {
            songAnalyticsAsyncService.processMultipleSongsAnalyticsAsync();
            return ResponseEntity.ok("Procesamiento masivo de analítica iniciado");
        } catch (Exception e) {
            throw new RuntimeException("Error al iniciar procesamiento masivo: " + e.getMessage());
        }
    }
}
