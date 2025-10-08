package com.misacordes.application.controller;

import com.misacordes.application.dto.request.ChordPosition;
import com.misacordes.application.dto.request.SongRequest;
import com.misacordes.application.dto.response.ChordInfo;
import com.misacordes.application.dto.response.SongResponse;
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
    public ResponseEntity<SongResponse> createdSong(@RequestBody SongRequest request){
        try {
            SongResponse response = songService.createSong(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            throw new RuntimeException("Failed to create the song" + e.getMessage());
        }

    }

    @PutMapping("/{id}")
    public ResponseEntity <SongResponse> updateSong(
            @PathVariable long id,
            @RequestBody SongRequest request){
        try {
            SongResponse response = songService.updateSong(id, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new RuntimeException("Failed to actualize song" + e);
        }

    }
    @GetMapping("/{id}")
    public ResponseEntity<SongResponse> getSongById(@PathVariable long id){
        return ResponseEntity.ok(songService.getSongById(id));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSong(@PathVariable long id){
        songService.deleteSong(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my")
    public ResponseEntity<List<SongResponse>> getMySongs(){
        try {
            List<SongResponse> songs = songService.getMySongs();
            return ResponseEntity.ok(songs);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get songs" + e);
        }
    }

    @GetMapping("/public")
    public ResponseEntity <List<SongResponse>> getPublicSongs(){
        try {
            List<SongResponse> songs = songService.getPublicSongs();
            return ResponseEntity.ok(songs);
        } catch (Exception e) {
            throw new RuntimeException("Failed to recuperated public songs" + e);
        }
    }
    @GetMapping("/search")
    public ResponseEntity<List<SongResponse>> searchSongs(@RequestParam String q) {
        try {
            List<SongResponse> songs = songService.searchPublicSongs(q);
            return ResponseEntity.ok(songs);
        } catch (Exception e) {
            throw new RuntimeException("Error al buscar canciones: " + e.getMessage());
        }
    }
    @PutMapping("/{id}/submit")
    public ResponseEntity<SongResponse> submitForApproval(@PathVariable Long id) {
        try {
            SongResponse response = songService.submitForApproval(id);
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
     * PUT /api/songs/{id}/chords
     * Actualizar solo las posiciones de acordes de una canción
     */
    @PutMapping("/{id}/chords")
    public ResponseEntity<SongResponse> updateChordPositions(
            @PathVariable Long id,
            @RequestBody List<ChordPosition> chordPositions) {
        try {
            SongResponse response = songService.updateChordPositions(id, chordPositions);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar acordes: " + e.getMessage());
        }
    }

}
