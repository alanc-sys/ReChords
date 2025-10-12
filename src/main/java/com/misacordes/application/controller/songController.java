package com.misacordes.application.controller;

import com.misacordes.application.dto.request.SongWithChordsRequest;
import com.misacordes.application.dto.response.ChordInfo;
import com.misacordes.application.dto.response.PageResponse;
import com.misacordes.application.dto.response.SongWithChordsResponse;
import com.misacordes.application.dto.response.SongAnalyticsResponse;
import com.misacordes.application.services.SongImportService;
import com.misacordes.application.services.ChordService;
import com.misacordes.application.services.SongService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/songs")
@RequiredArgsConstructor
public class SongController {
    private final SongService songService;
    private final ChordService chordService;
    private final SongImportService songImportService;

    @PostMapping
    public ResponseEntity<SongWithChordsResponse> createSong(@Valid @RequestBody SongWithChordsRequest request){
        SongWithChordsResponse response = songService.createSongWithChords(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SongWithChordsResponse> updateSong(
            @PathVariable long id,
            @Valid @RequestBody SongWithChordsRequest request){
        SongWithChordsResponse response = songService.updateSongWithChords(id, request);
        return ResponseEntity.ok(response);
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




    @PutMapping("/{id}/submit")
    public ResponseEntity<SongWithChordsResponse> submitForApproval(@PathVariable Long id) {
        SongWithChordsResponse response = songService.submitForApprovalWithChords(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/available-chords")
    public ResponseEntity<List<ChordInfo>> getAvailableChords() {
        List<ChordInfo> chords = chordService.getAllChordsForSelection();
        return ResponseEntity.ok(chords);
    }

    @GetMapping("/common-chords")
    public ResponseEntity<List<ChordInfo>> getCommonChords() {
        List<ChordInfo> chords = chordService.getCommonChordsForSelection();
        return ResponseEntity.ok(chords);
    }

    @GetMapping("/{id}/analytics")
    public ResponseEntity<SongAnalyticsResponse> getSongAnalytics(@PathVariable Long id) {
        SongAnalyticsResponse response = songService.getSongAnalytics(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/import")
    public ResponseEntity<SongWithChordsRequest> importSong(@RequestBody String rawText) {
        SongWithChordsRequest parsedRequest = songImportService.parse(rawText);
        return ResponseEntity.ok(parsedRequest);
    }
    @GetMapping("/{id}/transpose")
    public ResponseEntity<SongWithChordsResponse> transposeSong(
            @PathVariable Long id,
            @RequestParam int semitones) {
        SongWithChordsResponse response = songService.transposeSong(id, semitones);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    public ResponseEntity<PageResponse<SongWithChordsResponse>> getMySongsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort) {
        Pageable pageable = createPageable(page, size, sort);
        PageResponse<SongWithChordsResponse> response = songService.getMySongsWithChordsPaginated(pageable);
        return ResponseEntity.ok(response);
    }

        @GetMapping("/public")
        public ResponseEntity<PageResponse<SongWithChordsResponse>> getPublicSongsPaginated(
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "20") int size,
                @RequestParam(defaultValue = "publishedAt,desc") String[] sort) {
            Pageable pageable = createPageable(page, size, sort);
            PageResponse<SongWithChordsResponse> response = songService.getPublicSongsWithChordsPaginated(pageable);
            return ResponseEntity.ok(response);
        }

        @GetMapping("/search")
        public ResponseEntity<PageResponse<SongWithChordsResponse>> searchSongsPaginated(
                @RequestParam String q,
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "20") int size,
                @RequestParam(defaultValue = "title,asc") String[] sort) {
            Pageable pageable = createPageable(page, size, sort);
            PageResponse<SongWithChordsResponse> response = songService.searchPublicSongsWithChordsPaginated(q, pageable);
            return ResponseEntity.ok(response);
        }

    private Pageable createPageable(int page, int size, String[] sort) {
        if (size > 20) {
            size = 20;
        }

        if (size < 1) {
            size = 1;
        }

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
