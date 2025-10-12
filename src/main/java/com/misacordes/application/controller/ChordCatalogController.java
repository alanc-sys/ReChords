package com.misacordes.application.controller;

import com.misacordes.application.dto.response.ChordResponse;
import com.misacordes.application.services.ChordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chord-catalog")
@RequiredArgsConstructor
public class ChordCatalogController {
    
    private final ChordService chordService;
    
    @GetMapping
    public ResponseEntity<List<ChordResponse>> getAvailableChords() {
        return ResponseEntity.ok(chordService.getAllChords());
    }
}
