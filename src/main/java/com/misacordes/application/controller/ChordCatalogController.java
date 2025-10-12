package com.misacordes.application.controller;

import com.misacordes.application.entities.ChordCatalog;
import com.misacordes.application.repositories.ChordCatalogRepository;
import com.misacordes.application.services.ChordCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/chords")
@RequiredArgsConstructor
public class ChordCatalogController {

    private final ChordCatalogRepository chordCatalogRepository;
    private final ChordCatalogService chordCatalogService;

    @GetMapping
    public ResponseEntity<List<ChordCatalog>> getAllChords() {
        List<ChordCatalog> chords = chordCatalogRepository.findAllByOrderByDisplayOrderAsc();
        return ResponseEntity.ok(chords);
    }


    @GetMapping("/{name}")
    public ResponseEntity<ChordCatalog> getChordByName(@PathVariable String name) {
        Optional<ChordCatalog> chord = chordCatalogRepository.findByName(name);
        return chord.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/common")
    public ResponseEntity<List<ChordCatalog>> getCommonChords() {
        List<ChordCatalog> chords = chordCatalogRepository.findByIsCommonTrueOrderByDisplayOrderAsc();
        return ResponseEntity.ok(chords);
    }

    @PostMapping("/update-positions")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<String> updateFingerPositions() {
        try {
            chordCatalogService.updateChordFingerPositions();
            return ResponseEntity.ok("Finger positions updated successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error updating finger positions: " + e.getMessage());
        }
    }
    

    @GetMapping("/{name}/variations")
    public ResponseEntity<List<ChordCatalog>> getChordVariations(@PathVariable String name) {
        List<ChordCatalog> variations = new ArrayList<>();

        chordCatalogRepository.findByName(name).ifPresent(variations::add);

        for (int i = 1; i <= 10; i++) {
            String varName = name + "_var" + i;
            chordCatalogRepository.findByName(varName).ifPresent(variations::add);
        }
        
        if (variations.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(variations);
    }
}

