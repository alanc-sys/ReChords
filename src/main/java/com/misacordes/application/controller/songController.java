package com.misacordes.application.controller;

import com.misacordes.application.dto.request.SongWithChordsRequest;
import com.misacordes.application.dto.response.ChordInfo;
import com.misacordes.application.dto.response.SongWithChordsResponse;
import com.misacordes.application.dto.response.SongAnalyticsResponse;
import com.misacordes.application.services.auth.ChordService;
import com.misacordes.application.services.auth.SongService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/songs")
@RequiredArgsConstructor
public class songController {
    private final SongService songService;
    private final ChordService chordService;

    @PostMapping
    public ResponseEntity<SongWithChordsResponse> createSong(@RequestBody SongWithChordsRequest request){
        try {
            SongWithChordsResponse response = songService.createSongWithChords(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create the song: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<SongWithChordsResponse> updateSong(
            @PathVariable long id,
            @RequestBody SongWithChordsRequest request){
        try {
            SongWithChordsResponse response = songService.updateSongWithChords(id, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update song: " + e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<SongWithChordsResponse> getSongById(@PathVariable long id){
        return ResponseEntity.ok(songService.getSongWithChordsById(id));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSong(@PathVariable long id){
        songService.deleteSong(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my")
    public ResponseEntity<List<SongWithChordsResponse>> getMySongs(){
        try {
            List<SongWithChordsResponse> songs = songService.getMySongsWithChords();
            return ResponseEntity.ok(songs);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get songs: " + e.getMessage());
        }
    }

    @GetMapping("/public")
    public ResponseEntity<List<SongWithChordsResponse>> getPublicSongs(){
        try {
            List<SongWithChordsResponse> songs = songService.getPublicSongsWithChords();
            return ResponseEntity.ok(songs);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get public songs: " + e.getMessage());
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<SongWithChordsResponse>> searchSongs(@RequestParam String q) {
        try {
            List<SongWithChordsResponse> songs = songService.searchPublicSongsWithChords(q);
            return ResponseEntity.ok(songs);
        } catch (Exception e) {
            throw new RuntimeException("Error al buscar canciones: " + e.getMessage());
        }
    }
    
    @PutMapping("/{id}/submit")
    public ResponseEntity<SongWithChordsResponse> submitForApproval(@PathVariable Long id) {
        try {
            SongWithChordsResponse response = songService.submitForApprovalWithChords(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new RuntimeException("Error al enviar canción: " + e.getMessage());
        }
    }
    
    // ========== ENDPOINTS PARA ACORDES ==========
    
    /**
     * GET /api/songs/available-chords
     * Obtener todos los acordes disponibles para arrastrar
     */
    @GetMapping("/available-chords")
    public ResponseEntity<List<ChordInfo>> getAvailableChords() {
        try {
            List<ChordInfo> chords = chordService.getAllChordsForSelection();
            return ResponseEntity.ok(chords);
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener acordes: " + e.getMessage());
        }
    }
    
    /**
     * GET /api/songs/common-chords
     * Obtener solo acordes comunes para selección rápida
     */
    @GetMapping("/common-chords")
    public ResponseEntity<List<ChordInfo>> getCommonChords() {
        try {
            List<ChordInfo> chords = chordService.getCommonChordsForSelection();
            return ResponseEntity.ok(chords);
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener acordes comunes: " + e.getMessage());
        }
    }

    /**
     * GET /api/songs/{id}/analytics
     * Obtener analítica de una canción (estadísticas de acordes)
     */
    @GetMapping("/{id}/analytics")
    public ResponseEntity<SongAnalyticsResponse> getSongAnalytics(@PathVariable Long id) {
        try {
            SongAnalyticsResponse response = songService.getSongAnalytics(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener analítica de la canción: " + e.getMessage());
        }
    }

}
