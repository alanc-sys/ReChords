package com.misacordes.application.controller;

import com.misacordes.application.dto.request.SongRequest;
import com.misacordes.application.dto.response.SongResponse;
import com.misacordes.application.services.auth.SongService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/songs")
@RequiredArgsConstructor
public class songController {
    private final SongService songService;

    @PostMapping
    public ResponseEntity<SongResponse> createdSong(@RequestBody SongRequest request){
        return ResponseEntity.ok(songService.createSong((request)));
    }

    @GetMapping
    public ResponseEntity<List<SongResponse>> getAllSongs(){
        return ResponseEntity.ok(songService.getAllSongs());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SongResponse> songDetail(@PathVariable Long id){
        return ResponseEntity.ok(songService.songDetail(id));
    }
    @DeleteMapping("{id}")
    public ResponseEntity<SongResponse> deleteSong(@PathVariable Long id){
        songService.deleteSong(id);
        return ResponseEntity.noContent().build();
    }

}
