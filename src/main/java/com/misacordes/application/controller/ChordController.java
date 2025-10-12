package com.misacordes.application.controller;

import com.misacordes.application.dto.response.ChordResponse;
import com.misacordes.application.dto.request.ChordRequest;
import com.misacordes.application.entities.ChordCategory;
import com.misacordes.application.entities.DifficultyLevel;
import com.misacordes.application.services.ChordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

// import javax.validation.Valid; // Comentado temporalmente
import java.util.List;

@RestController
@RequestMapping("/api/chords")
@RequiredArgsConstructor
public class ChordController {

    private final ChordService chordService;


    @GetMapping
    public ResponseEntity<List<ChordResponse>> getAllChords() {
        List<ChordResponse> chords = chordService.getAllChords();
        return ResponseEntity.ok(chords);
    }

    @GetMapping("/common")
    public ResponseEntity<List<ChordResponse>> getCommonChords() {
        List<ChordResponse> chords = chordService.getCommonChords();
        return ResponseEntity.ok(chords);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<ChordResponse>> getChordsByCategory(@PathVariable ChordCategory category) {
        List<ChordResponse> chords = chordService.getChordsByCategory(category);
        return ResponseEntity.ok(chords);
    }

    @GetMapping("/difficulty/{level}")
    public ResponseEntity<List<ChordResponse>> getChordsByDifficulty(@PathVariable DifficultyLevel level) {
        List<ChordResponse> chords = chordService.getChordsByDifficulty(level);
        return ResponseEntity.ok(chords);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChordResponse> getChordById(@PathVariable Long id) {
        ChordResponse chord = chordService.getChordById(id);
        return ResponseEntity.ok(chord);
    }

    // ========== ENDPOINTS ADMIN ==========

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ChordResponse> createChord(@RequestBody ChordRequest request) {
        ChordResponse response = chordService.createChord(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ChordResponse> updateChord(
            @PathVariable Long id,
            @RequestBody ChordRequest request) {
        ChordResponse response = chordService.updateChord(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteChord(@PathVariable Long id) {
        chordService.deleteChord(id);
        return ResponseEntity.noContent().build();
    }
}
