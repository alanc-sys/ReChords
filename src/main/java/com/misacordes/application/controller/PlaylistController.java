package com.misacordes.application.controller;

import com.misacordes.application.dto.request.AddSongToPlaylistRequest;
import com.misacordes.application.dto.request.CreatePlaylistRequest;
import com.misacordes.application.dto.request.UpdatePlaylistRequest;
import com.misacordes.application.dto.response.PlaylistResponse;
import com.misacordes.application.dto.response.PlaylistSummaryResponse;
import com.misacordes.application.services.PlaylistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/playlists")
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistService playlistService;

    /**
     * POST /api/playlists
     * Crear una nueva playlist
     */
    @PostMapping
    public ResponseEntity<PlaylistResponse> createPlaylist(@RequestBody CreatePlaylistRequest request) {
        try {
            PlaylistResponse response = playlistService.createPlaylist(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            throw new RuntimeException("Error al crear playlist: " + e.getMessage());
        }
    }

    /**
     * GET /api/playlists/my
     * Obtener todas las playlists del usuario actual
     */
    @GetMapping("/my")
    public ResponseEntity<List<PlaylistSummaryResponse>> getMyPlaylists() {
        try {
            List<PlaylistSummaryResponse> playlists = playlistService.getMyPlaylists();
            return ResponseEntity.ok(playlists);
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener playlists: " + e.getMessage());
        }
    }

    /**
     * GET /api/playlists/{id}
     * Obtener una playlist específica con sus canciones
     */
    @GetMapping("/{id}")
    public ResponseEntity<PlaylistResponse> getPlaylistById(@PathVariable Long id) {
        try {
            PlaylistResponse playlist = playlistService.getPlaylistById(id);
            return ResponseEntity.ok(playlist);
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener playlist: " + e.getMessage());
        }
    }

    /**
     * PUT /api/playlists/{id}
     * Actualizar una playlist
     */
    @PutMapping("/{id}")
    public ResponseEntity<PlaylistResponse> updatePlaylist(
            @PathVariable Long id,
            @RequestBody UpdatePlaylistRequest request) {
        try {
            PlaylistResponse response = playlistService.updatePlaylist(id, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar playlist: " + e.getMessage());
        }
    }

    /**
     * DELETE /api/playlists/{id}
     * Eliminar una playlist
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlaylist(@PathVariable Long id) {
        try {
            playlistService.deletePlaylist(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar playlist: " + e.getMessage());
        }
    }

    /**
     * POST /api/playlists/{id}/songs
     * Añadir una canción a una playlist
     */
    @PostMapping("/{id}/songs")
    public ResponseEntity<PlaylistResponse> addSongToPlaylist(
            @PathVariable Long id,
            @RequestBody AddSongToPlaylistRequest request) {
        try {
            PlaylistResponse response = playlistService.addSongToPlaylist(id, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new RuntimeException("Error al añadir canción a playlist: " + e.getMessage());
        }
    }

    /**
     * DELETE /api/playlists/{id}/songs/{songId}
     * Eliminar una canción de una playlist
     */
    @DeleteMapping("/{id}/songs/{songId}")
    public ResponseEntity<PlaylistResponse> removeSongFromPlaylist(
            @PathVariable Long id,
            @PathVariable Long songId) {
        try {
            PlaylistResponse response = playlistService.removeSongFromPlaylist(id, songId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar canción de playlist: " + e.getMessage());
        }
    }

    /**
     * GET /api/playlists/public
     * Obtener playlists públicas para explorar
     */
    @GetMapping("/public")
    public ResponseEntity<List<PlaylistSummaryResponse>> getPublicPlaylists() {
        try {
            List<PlaylistSummaryResponse> playlists = playlistService.getPublicPlaylists();
            return ResponseEntity.ok(playlists);
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener playlists públicas: " + e.getMessage());
        }
    }

    /**
     * GET /api/playlists/search?q=query
     * Buscar playlists públicas por nombre
     */
    @GetMapping("/search")
    public ResponseEntity<List<PlaylistSummaryResponse>> searchPublicPlaylists(@RequestParam String q) {
        try {
            List<PlaylistSummaryResponse> playlists = playlistService.searchPublicPlaylists(q);
            return ResponseEntity.ok(playlists);
        } catch (Exception e) {
            throw new RuntimeException("Error al buscar playlists: " + e.getMessage());
        }
    }
}
