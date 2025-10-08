package com.misacordes.application.controller;


import com.misacordes.application.dto.request.RejectSongRequest;
import com.misacordes.application.dto.response.AdminStatsResponse;
import com.misacordes.application.dto.response.SongResponse;
import com.misacordes.application.services.auth.SongService;
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

    @GetMapping("/songs/pending")
    public ResponseEntity<List<SongResponse>> getPendingSongs() {
        try {
            List<SongResponse> songs = songService.getPendingSongs();
            return ResponseEntity.ok(songs);
        } catch (Exception e) {
            throw new RuntimeException("Failed to obtain Songs" + e);
        }
    }

    @GetMapping("/songs")
    public ResponseEntity<List<SongResponse>> getAllSongs() {
        try {
            List<SongResponse> songs = songService.getAllSongsAdmin();
            return ResponseEntity.ok(songs);
        } catch (Exception e) {
            throw new RuntimeException("Failed to obtain Songs" + e);
        }
    }

    @PutMapping("/songs/{id}/approve")
    public ResponseEntity<SongResponse> approveSong(@PathVariable Long id) {
        try {
            SongResponse response = songService.approveSong(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new RuntimeException("Error al aprobar canción: " + e.getMessage());
        }
    }

    @PutMapping("/songs/{id}/reject")
    public ResponseEntity<SongResponse> rejectSong(
            @PathVariable Long id,
            @RequestBody RejectSongRequest request) {
        try {
            SongResponse response = songService.rejectSong(id, request.getReason());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new RuntimeException("Error al rechazar canción: " + e.getMessage());
        }
    }

    @PutMapping("/songs/{id}/unpublish")
    public ResponseEntity<SongResponse> unpublishSong(@PathVariable Long id) {
        try {
            SongResponse response = songService.unpublishSong(id);
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
}
